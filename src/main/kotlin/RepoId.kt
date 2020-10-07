import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = RepoId.Companion::class)
data class RepoId(
    val owner: String,
    val name: String
) {
    companion object : KSerializer<RepoId> {
        private const val IDENTIFIER = "[A-Za-z0-9-_]+"
        private val ID = Regex("^($IDENTIFIER)/($IDENTIFIER)$")
        private val SSH = Regex("^git@github\\.com:($IDENTIFIER)/($IDENTIFIER)\\.git$")
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

        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("RepoId", PrimitiveKind.STRING)
        override fun serialize(encoder: Encoder, value: RepoId) = encoder.encodeString(value.toString())
        override fun deserialize(decoder: Decoder): RepoId = parse(decoder.decodeString())
    }

    override fun toString(): String = "$owner/$name"
}
