package net.im45.bot.grw

import com.google.gson.JsonElement
import net.im45.bot.grw.github.Release
import net.im45.bot.grw.github.RepoId
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

internal object Parser {

    private val FMT = DateTimeFormatter.ISO_DATE_TIME
    private val JsonElement.nullableAsString get() = if (isJsonNull) "(null)" else asString

    fun hasErrors(jsonElement: JsonElement) = jsonElement.asJsonObject.has("errors")

    fun hasData(jsonElement: JsonElement) = jsonElement.asJsonObject.has("data")

    fun getRepositories(jsonElement: JsonElement, repos: Set<RepoId>): Map<RepoId, JsonElement> {
        val map = mutableMapOf<RepoId, JsonElement>()
        val data = jsonElement.asJsonObject.getAsJsonObject("data")
        repos.forEach { map[it] = data.get(it.toLegalId()) }
        return map
    }

    fun parseReleases(jsonElement: JsonElement): Release? {
        if (jsonElement.isJsonNull) throw RepositoryException("Repository is null")

        val releaseNode = jsonElement.asJsonObject
            .getAsJsonObject("releases")
            .getAsJsonArray("nodes")
        if (releaseNode.size() == 0) return null
        val release = releaseNode.get(0).asJsonObject
        val author = release.getAsJsonObject("author")
        val assets = release.getAsJsonObject("releaseAssets").getAsJsonArray("nodes")

        return Release(
            release.get("name").nullableAsString,
            release.get("url").asString,
            release.get("tagName").asString,
            OffsetDateTime.parse(release.get("createdAt").asString, FMT).atZoneSameInstant(ZoneId.systemDefault()),
            OffsetDateTime.parse(release.get("updatedAt").asString, FMT).atZoneSameInstant(ZoneId.systemDefault()),
            Release.Author(
                // Strictly speaking, `Author` is marked nullable in GitHub API docs
                // But why..? Can you get a release out of thin air?
                author.get("name").nullableAsString,
                author.get("login").asString
            ),
            assets.map {
                it.asJsonObject.run {
                    Release.Asset(
                        get("name").asString,
                        get("size").asLong,
                        get("downloadUrl").asString
                    )
                }
            }.toList()
        )
    }

    fun filterNewVer(
        ver: RMutableMap<String>,
        repos: Map<RepoId, JsonElement>
    ): Pair<RMap<Release>, Set<RepoId>> {
        val map: RMutableMap<Release> = mutableMapOf()
        val nonexistent = mutableSetOf<RepoId>()

        ver.forEach { (r, p) ->
            val release: Release
            try {
                release = parseReleases(repos.getValue(r)) ?: let {
                    ver[r] = "-" to p.second
                    return@forEach
                }
            } catch (e: RepositoryException) {
                nonexistent.add(r)
                return@forEach
            }

            if (release.tagName != p.first) {
                ver[r] = release.tagName to p.second
                if (p.first != "?") {
                    map[r] = release to p.second
                }
            }
        }

        return map to nonexistent
    }
}
