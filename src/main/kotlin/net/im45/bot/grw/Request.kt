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
    private const val FRAG = "fragment latestRelease on Repository {releases(last: 1, orderBy: {field: CREATED_AT, direction: ASC}) {nodes {name url tagName createdAt publishedAt author {name login} releaseAssets(first: 100) {totalCount nodes {name size downloadUrl}}}}}"
    private const val FMT = "%s: repository(owner: \\\"%s\\\", name: \\\"%s\\\") { ...latestRelease } "

    private val TOKEN_REGEX = Regex("[0-9a-fA-F]{40}")
    private val ENDPOINT = URL("https://api.github.com/graphql")

    private fun buildQueryString(repos: Set<RepoId>): String {
        if (repos.isEmpty()) throw IllegalArgumentException("Can't build query from an empty set")

        val sb = StringBuilder("{")
        for (repo in repos) {
            sb.append(String.format(FMT, toLegalId(repo), repo.name, repo.owner))
        }
        sb.append("} ").append(FRAG)
        return sb.toString()
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
            Watcher.logger.error(toString())
            Watcher.logger.debug(jsonElement.toString())
        }

    }
}
