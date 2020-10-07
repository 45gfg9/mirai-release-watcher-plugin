import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.AutoSavePluginData
import net.mamoe.mirai.console.data.value

class GrwData : AutoSavePluginData("data")

object GrwSettings : AutoSavePluginConfig("settings") {
    var token by value("unset")
    var interval by value(30 * 1000)
    var timeout by value(15 * 1000)
    var autostart by value(false)
}

object GrwWatches : AutoSavePluginConfig("watches") {
    val watches: MutableMap<RepoId, Unit> by value()
}
