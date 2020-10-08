package net.im45.bot.grw

import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.AutoSavePluginData
import net.mamoe.mirai.console.data.value

class GrwData : AutoSavePluginData("data")

object GrwSettings : AutoSavePluginConfig("settings") {
    var token: String by value("unset")
    var interval: Int by value(30 * 1000)
    var timeout: Int by value(15 * 1000)
    var autostart: Boolean by value(false)
}

object GrwWatches : AutoSavePluginConfig("watches") {
    val watches: MutableMap<RepoId, String> by value(HashMap())
}
