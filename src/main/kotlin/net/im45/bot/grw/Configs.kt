package net.im45.bot.grw

import kotlinx.coroutines.Job
import net.im45.bot.grw.github.RepoId
import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.value

object GrwData {
    const val FRAGMENT = "fragment latestRelease on Repository {releases(last: 1, orderBy: {field: CREATED_AT, direction: ASC}) {nodes {name url tagName createdAt publishedAt author {name login} releaseAssets(first: 100) {totalCount nodes {name size downloadUrl}}}}}"

    var tokenBuf: String? = null
    var grwJob: Job? = null
}

object GrwSettings : AutoSavePluginConfig("settings") {
    var token: String by value("unset")
    var interval: Long by value(30 * 1000L)
    var timeout: Long by value(15 * 1000L)
    var autostart: Boolean by value(false)
}

object GrwWatches : AutoSavePluginConfig("watches") {
    val watches: MutableMap<RepoId, Pair<String, Set<Long>>> by value(mutableMapOf())
}
