data class RepoId(
    val owner: String,
    val name: String
) {
    companion object {
        private val ID = Regex("^([A-Za-z0-9-_]+)/([A-Za-z0-9-_]+)$")
        private val SSH = Regex("^git@github\\.com:([A-Za-z0-9-_]+)/([A-Za-z0-9-_]+)\\.git$")
        private val HTTPS = Regex("^https://github\\.com/([A-Za-z0-9-_]+)/([A-Za-z0-9-_]+)(?:\\.git)?$")

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

    override fun toString(): String = "$owner/$name"
}
