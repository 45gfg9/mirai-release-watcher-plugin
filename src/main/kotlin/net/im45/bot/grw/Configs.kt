package net.im45.bot.grw

import kotlinx.coroutines.Job
import net.im45.bot.grw.github.RepoId
import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.value

object GrwData {
    var tokenBuf: String? = null
    var grwJob: Job? = null
}

object GrwSettings : AutoSavePluginConfig("settings") {
    var botId: Long by value(0L)
    var token: String by value("unset")
    var interval: Long by value(30 * 1000L)
    var timeout: Long by value(15 * 1000L)
    var autostart: Boolean by value(false)
}

object GrwWatches : AutoSavePluginConfig("watches") {
    val watches: MutableMap<RepoId, Pair<String, Set<Long>>> by value(mutableMapOf())
}
