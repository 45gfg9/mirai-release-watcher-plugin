package net.im45.bot.grw

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import net.im45.bot.grw.github.Release
import net.im45.bot.grw.github.RepoId
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private fun JsonElement.nullableAsString() = if (isJsonNull) null.toString() else asString

internal object Parser {
    private val FMT = DateTimeFormatter.ISO_DATE_TIME

    fun hasErrors(jsonElement: JsonElement) = jsonElement.asJsonObject.has("errors")

    fun hasData(jsonElement: JsonElement) = jsonElement.asJsonObject.has("data")

    fun handleError(jsonElement: JsonElement, block: JsonArray.() -> Unit) =
            jsonElement.asJsonObject.getAsJsonArray("errors").run(block)

    fun getRepositories(jsonElement: JsonElement, repos: Set<RepoId>): Map<RepoId, JsonElement> {
        val map = mutableMapOf<RepoId, JsonElement>()
        val data = jsonElement.asJsonObject.getAsJsonObject("data")
        repos.forEach { map[it] = data.get(toLegalId(it)) }
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
        val assetsConn = release.getAsJsonObject("releaseAssets")
        val assetsCount = assetsConn.get("totalCount").asInt
        val assets = assetsConn.getAsJsonArray("nodes")

        val assetsList = ArrayList<Release.Asset>(assetsCount)
        assets.forEach {
            val assetObj = it.asJsonObject
            val asset = Release.Asset(
                    assetObj.get("name").asString,
                    assetObj.get("size").asLong,
                    assetObj.get("downloadUrl").asString
            )
            assetsList.add(asset)
        }

        return Release(
                release.get("name").asString,
                release.get("url").asString,
                release.get("tagName").asString,
                OffsetDateTime.parse(release.get("createdAt").asString, FMT).atZoneSameInstant(ZoneId.systemDefault()),
                OffsetDateTime.parse(release.get("publishedAt").asString, FMT).atZoneSameInstant(ZoneId.systemDefault()),
                Release.Author(
                        author.get("name").nullableAsString(),
                        author.get("login").asString
                ),
                assetsList
        )
    }

    fun filterNewVer(ver: MutableMap<RepoId, Pair<String, Set<Long>>>, repos: Map<RepoId, JsonElement>): Map<RepoId, Pair<Release, Set<Long>>> {
        val map = mutableMapOf<RepoId, Pair<Release, Set<Long>>>()
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
        nonexistent.forEach(ver::remove)

        return map
    }
}
