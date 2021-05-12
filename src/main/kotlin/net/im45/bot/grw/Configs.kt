package net.im45.bot.grw

import net.im45.bot.grw.github.RepoId
import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.value

internal typealias RMap<T> = Map<RepoId, Pair<T, Set<Long>>>
internal typealias RMutableMap<T> = MutableMap<RepoId, Pair<T, Set<Long>>>

object GrwSettings : AutoSavePluginConfig("settings") {
    var enabled: Boolean by value(false)
    var token: String by value("unset")
    var interval: Long by value(30 * 1000L)
    var timeout: Long by value(15 * 1000L)
}

object GrwWatches : AutoSavePluginConfig("watches") {
    val watches: RMutableMap<String> by value(mutableMapOf())
}
