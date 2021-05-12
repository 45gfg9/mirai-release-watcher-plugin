package net.im45.bot.grw.github

import java.time.ZonedDateTime
import kotlin.math.log10
import kotlin.math.pow

internal data class Release(
    val name: String,
    val url: String,
    val tagName: String,
    val createdAt: ZonedDateTime,
    val updatedAt: ZonedDateTime,
    val author: Author,
    val releaseAssets: List<Asset>
) {
    internal data class Author(val name: String, val login: String) {
        override fun toString() = "$name ($login)"
    }

    internal data class Asset(val name: String, val size: Long, val downloadUrl: String) {
        private companion object {
            private val SUFFIXES = charArrayOf('K', 'M', 'G', 'T', 'P', 'E', 'Z', 'Y', 'B', 'N', 'D')
        }

        internal val sizeString = (log10(size.toDouble()) / 3).toInt().let {
            if (it == 0)
                "${size}B"
            else
                String.format("%.2f", size / 1e3.pow(it)) + SUFFIXES[it - 1] + "B"
        }
    }
}
