package net.im45.bot.grw

class RepoIdFormatException : IllegalArgumentException {
    constructor() : super()
    constructor(s: String) : super(s)

    companion object {
        fun forInputString(url: String) = RepoIdFormatException("For input string: \"$url\"")
    }
}

class RepositoryException : Exception {
    constructor() : super()
    constructor(s: String) : super(s)
}
