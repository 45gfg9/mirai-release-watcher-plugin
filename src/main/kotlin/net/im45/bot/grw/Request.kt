package net.im45.bot.grw

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonIOException
import com.google.gson.JsonParser
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.*
import io.ktor.client.features.auth.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.*
import net.im45.bot.grw.github.RepoId
import net.im45.bot.grw.ktor.bearer
import java.io.Closeable

object Request : Closeable {
    private const val FRAGMENT =
        "fragment latestRelease on Repository {releases(last: 1, orderBy: {field: CREATED_AT, direction: ASC}) {nodes {name url tagName createdAt updatedAt author {name login} releaseAssets(first: 100) {nodes {name size downloadUrl}}}}}"
    private const val TEMPLATE = "%s: repository(owner: \\\"%s\\\", name: \\\"%s\\\") { ...latestRelease } "

    private val ENDPOINT = Url("https://api.github.com/graphql")

    private val ioScope = CoroutineScope(Dispatchers.IO)

    private lateinit var job: Job
    private lateinit var httpClient: HttpClient

    var tokenBuf: String? = null

    private fun buildQueryString(repos: Set<RepoId>): String {
        require(repos.isNotEmpty()) { "Can't build query from an empty set" }
        return buildString {
            append("{")
            repos.forEach { append(TEMPLATE.format(it.toLegalId(), it.name, it.owner)) }
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
                if (GrwSettings.enabled)
                    Request()
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

        HttpClient(CIO) {
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

    suspend operator fun invoke() {
        assert(::job.isInitialized)

        if (GrwWatches.watches.isEmpty() || !::httpClient.isInitialized) return

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

        jsonElement.handleError {
            Watcher.logger.error("Error received from upstream")
            Watcher.logger.error(it.toString())
            Watcher.logger.debug(toString())
        }
    }

    private inline fun JsonElement.handleError(block: JsonElement.(JsonArray) -> Unit) =
        block(asJsonObject.getAsJsonArray("error"))

}
