package net.im45.bot.grw.github

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.im45.bot.grw.RepoIdFormatException

@Serializable(with = RepoId.Serializer::class)
data class RepoId(
    val owner: String,
    val name: String
) {
    companion object {
        private const val IDENTIFIER = "[A-Za-z0-9-_]+"

        @JvmStatic
        private val ID = Regex("^($IDENTIFIER)/($IDENTIFIER)$")

        @JvmStatic
        private val SSH = Regex("^git@github\\.com:($IDENTIFIER)/($IDENTIFIER)\\.git$")

        @JvmStatic
        private val HTTPS = Regex("^https://github\\.com/($IDENTIFIER)/($IDENTIFIER)(?:\\.git)?$")

        @JvmStatic
        fun parse(url: String): RepoId {
            val match = ID.find(url)
                ?: SSH.find(url)
                ?: HTTPS.find(url)
                ?: throw RepoIdFormatException.forInputString(url)

            val owner = match.groupValues[1]
            val name = match.groupValues[2]

            return RepoId(owner, name)
        }
    }

    object Serializer : KSerializer<RepoId> {
        override val descriptor: SerialDescriptor =
            PrimitiveSerialDescriptor("net.im45.bot.grw.github.RepoId", PrimitiveKind.STRING)

        override fun serialize(encoder: Encoder, value: RepoId) = encoder.encodeString(value.toString())

        override fun deserialize(decoder: Decoder): RepoId = parse(decoder.decodeString())
    }

    override fun toString(): String = "$owner/$name"

    fun toLegalId() = toString()
        .replaceFirst(Regex("^(\\d)"), "_$1")
        .replace(Regex("[-/.]"), "_")
}
