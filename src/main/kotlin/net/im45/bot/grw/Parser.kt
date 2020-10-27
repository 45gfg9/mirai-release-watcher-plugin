package net.im45.bot.grw

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import net.im45.bot.grw.github.Release
import net.im45.bot.grw.github.RepoId
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

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

        val assetsList = mutableListOf<Release.Asset>()
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
                LocalDateTime.parse(release.get("createdAt").asString, FMT),
                LocalDateTime.parse(release.get("publishedAt").asString, FMT),
                Release.Author(
                        release.get("name").asString,
                        release.get("login").asString
                ),
                assetsList
        )
    }
}
