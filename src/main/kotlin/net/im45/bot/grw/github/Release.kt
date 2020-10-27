package net.im45.bot.grw.github

import java.time.LocalDateTime

internal data class Release(
        val name: String,
        val url: String,
        val tagName: String,
        val createdAt: LocalDateTime,
        val publishedAt: LocalDateTime,
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
