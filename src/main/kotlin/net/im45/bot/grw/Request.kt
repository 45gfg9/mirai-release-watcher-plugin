package net.im45.bot.grw

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonIOException
import com.google.gson.JsonParser
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.features.*
import io.ktor.client.features.auth.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.*
import net.im45.bot.grw.github.RepoId
import net.im45.bot.grw.ktor.bearer
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.console.util.ContactUtils.getContactOrNull
import java.io.Closeable

object Request : Closeable {
    private const val FRAGMENT =
        "fragment latestRelease on Repository {releases(last: 1, orderBy: {field: CREATED_AT, direction: ASC}) {nodes {name url tagName createdAt updatedAt author {name login} releaseAssets(first: 100) {nodes {name size downloadUrl}}}}}"
    private const val TEMPLATE = "%s: repository(owner: \\\"%s\\\", name: \\\"%s\\\") { ...latestRelease } "

    private val logger get() = Watcher.logger

    private val ENDPOINT = Url("https://api.github.com/graphql")

    private val ioScope = CoroutineScope(Dispatchers.IO)

    private lateinit var job: Job
    private lateinit var httpClient: HttpClient

    var tokenBuf: String? = null

    private fun buildQueryString(repos: Set<RepoId>): String {
        require(repos.isNotEmpty()) { "Can't build query from an empty set" }
        return buildString {
            append("{")
            repos.forEach { append(TEMPLATE.format(it.toLegalId(), it.owner, it.name)) }
            append("} ", FRAGMENT)
        }
    }

    private fun closeHttpClient() {
        if (::httpClient.isInitialized)
            httpClient.close()
    }

    fun init() {
        if (tokenBuf == null && GrwSettings.token.length != 40) {
            Watcher.logger.warning("To use GRW, you need to set token first.")
            return
        }

        tokenBuf?.let { ioScope.launch { verifyToken(it) } }

        job = ioScope.launch {
            while (true) {
                if (GrwSettings.enabled) runCatching { Request() }.onFailure { logger.error(it) }
                delay(GrwSettings.interval)
            }
        }
    }

    override fun close() {
        if (::job.isInitialized)
            job.cancel()
        closeHttpClient()
    }

    suspend fun verifyToken(token: String): Boolean {
        if (token != tokenBuf) {
            tokenBuf = token
        }

        HttpClient(OkHttp) {
            install(Auth) {
                bearer {
                    this.token = token
                }
            }
            install(HttpTimeout) {
                socketTimeoutMillis = GrwSettings.timeout
            }
        }.run {
            runCatching { post<Unit>(ENDPOINT) }.getOrNull()?.let {
                // successfully returned
                tokenBuf = null
                GrwSettings.token = token

                closeHttpClient()
                httpClient = this

                return true
            } ?: run {
                // exception occurred, request fail
                close()

                return false
            }
        }
    }

    @OptIn(ConsoleExperimentalApi::class)
    suspend operator fun invoke() {
        assert(::job.isInitialized)

        if (GrwWatches.watches.isEmpty() || !::httpClient.isInitialized) return
        logger.verbose("Starting a new request..")

        val sb = buildQueryString(GrwWatches.watches.keys)

        val jsonElement = runCatching {
            httpClient.post<String>(ENDPOINT) {
                body = "{\"query\":\"$sb\"}"
            }.let(JsonParser::parseString)
        }.getOrElse {
            if (it is ClientRequestException || it is JsonIOException) {
                Watcher.logger.error(it)
                return
            } else throw it
        }

        if (Parser.hasErrors(jsonElement))
            jsonElement.handleError {
                logger.error("Error received from upstream")
                logger.error(it.toString())
                logger.debug(toString())
            }

        if (!Parser.hasData(jsonElement)) {
            logger.warning("Received object has no \"data\" value")
            return
        }

        val repos = Parser.getRepositories(jsonElement, GrwWatches.watches.keys)
        val (newReleases, nonExistence) = Parser.filterNewVer(GrwWatches.watches, repos)

        logger.verbose(repos.toString())

        // TODO handle nonexistent repos
        newReleases.forEach { (n, p) ->
            logger.verbose("Processing $n")
            buildString {
                p.first.run {
                    appendLine("New release found for $n!")
                    appendLine("URL: $url")
                    appendLine("Name: $name")
                    appendLine("Tag Name: $tagName")
                    appendLine("Created At: $createdAt")
                    appendLine("Updated At: $updatedAt")
                    appendLine("Author: $author")
                    appendLine("This release has ${releaseAssets.size} asset(s).")
                    releaseAssets.forEach {
                        appendLine("--------------------")
                        appendLine("File name: ${it.name}")
                        appendLine("Size: ${it.sizeString()}")
                        appendLine("Download URL: ${it.downloadUrl}")
                    }
                }
            }.trim().let { msg ->
                Bot.getInstance(GrwSettings.botId).run {
                    p.second.forEach {
                        logger.verbose("Sending to $it")
                        getContactOrNull(it)?.sendMessage(msg) ?: logger.warning("Contact $it is null")
                    }
                }
            }
        }
    }

    private inline fun JsonElement.handleError(block: JsonElement.(JsonArray) -> Unit) =
        block(asJsonObject.getAsJsonArray("errors"))
}
