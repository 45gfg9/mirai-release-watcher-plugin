package net.im45.bot.grw

import com.google.gson.JsonIOException
import com.google.gson.JsonParser
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.*
import io.ktor.client.features.auth.*
import io.ktor.client.request.*
import net.im45.bot.grw.github.RepoId
import net.im45.bot.grw.ktor.bearer
import java.net.URL

object Request {
    private const val FRAGMENT = "fragment latestRelease on Repository {releases(last: 1, orderBy: {field: CREATED_AT, direction: ASC}) {nodes {name url tagName createdAt publishedAt author {name login} releaseAssets(first: 100) {totalCount nodes {name size downloadUrl}}}}}"
    private const val TEMPLATE = "%s: repository(owner: \\\"%s\\\", name: \\\"%s\\\") { ...latestRelease } "

    private val ENDPOINT = URL("https://api.github.com/graphql")

    private fun buildQueryString(repos: Set<RepoId>): String {

        return if (repos.isEmpty()) buildString {
            append("{")
            repos.forEach { append(TEMPLATE.format(toLegalId(it), it.name, it.owner)) }
            append("} ", FRAGMENT)
        } else throw IllegalArgumentException("Can't build query from an empty set")
    }

    suspend fun verifyToken(token: String): Boolean {
        GrwData.tokenBuf = token
        val ret = HttpClient {
            install(Auth) {
                bearer {
                    this.token = token
                }
            }
        }.use {
            runCatching { it.post<Unit>(ENDPOINT) }.getOrNull() != null
        }
        if (ret) {
            GrwData.tokenBuf = null
            GrwSettings.token = token
        }
        return ret
    }

    suspend fun request() {
        if (GrwWatches.watches.isEmpty()) return

        val sb = buildQueryString(GrwWatches.watches.keys)

        val jsonElement = HttpClient(CIO) {
            install(Auth) {
                bearer {
                    token = GrwSettings.token
                }
            }
        }.use {
            try {
                it.post<String>(ENDPOINT) {
                    body = "{\"query\":\"$sb\"}"
                }.let(JsonParser::parseString)
            } catch (e: Exception) {
                // no multi-catch...
                when (e) {
                    is ClientRequestException, is JsonIOException -> {
                        Watcher.logger.error(e)
                        return@request
                    }
                    else -> throw e
                }
            }
        }

        Parser.handleError(jsonElement) {
            Watcher.logger.error("Error received from upstream")
            Watcher.logger.error(it.toString())
            Watcher.logger.debug(toString())
        }

    }
}
