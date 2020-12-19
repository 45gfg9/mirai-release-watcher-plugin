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
import net.im45.bot.grw.github.RepoId
import net.im45.bot.grw.ktor.bearer
import java.net.URL

object Request {
    private const val FRAGMENT =
        "fragment latestRelease on Repository {releases(last: 1, orderBy: {field: CREATED_AT, direction: ASC}) {nodes {name url tagName createdAt updatedAt author {name login} releaseAssets(first: 100) {nodes {name size downloadUrl}}}}}"
    private const val TEMPLATE = "%s: repository(owner: \\\"%s\\\", name: \\\"%s\\\") { ...latestRelease } "

    private val ENDPOINT = URL("https://api.github.com/graphql")

    private fun buildQueryString(repos: Set<RepoId>): String {
        require(repos.isNotEmpty()) { "Can't build query from an empty set" }
        return buildString {
            append("{")
            repos.forEach { append(TEMPLATE.format(it.toLegalId(), it.name, it.owner)) }
            append("} ", FRAGMENT)
        }
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
            runCatching {
                it.post<String>(ENDPOINT) {
                    body = "{\"query\":\"$sb\"}"
                }.let(JsonParser::parseString)
            }.getOrElse {
                if (it is ClientRequestException || it is JsonIOException) {
                    Watcher.logger.error(it)
                    return
                } else throw it
            }
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
