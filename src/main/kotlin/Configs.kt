import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.AutoSavePluginData
import net.mamoe.mirai.console.data.value

class GrwData : AutoSavePluginData()

object GrwSetting : AutoSavePluginConfig() {
    var token by value("unset")
    var interval by value(30 * 1000)
    var timeout by value(15 * 1000)
    var autostart by value(false)
}

object GrwWatches : AutoSavePluginConfig() {
    val watches: Map<RepoId, Unit> by value()
}
