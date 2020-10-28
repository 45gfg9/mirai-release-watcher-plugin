package net.im45.bot.grw.github

import java.time.ZonedDateTime

internal data class Release(
        val name: String,
        val url: String,
        val tagName: String,
        val createdAt: ZonedDateTime,
        val publishedAt: ZonedDateTime,
        val author: Author,
        val releaseAssets: List<Asset>
) {
    internal data class Author(
            val name: String,
            val login: String
    )

    internal data class Asset(
            val name: String,
            val size: Long,
            val downloadUrl: String
    )
}
