import com.google.auto.service.AutoService
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.CompositeCommand
import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.AutoSavePluginData
import net.mamoe.mirai.console.data.value
import net.mamoe.mirai.console.plugin.jvm.JvmPlugin
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin

@AutoService(JvmPlugin::class)
object Watcher : KotlinPlugin(
    JvmPluginDescription(
        "net.im45.bot.grw",
        "1.0-alpha-01",
        "GitHub Release Watcher"
    )
) {
    override fun onEnable() {
        super.onEnable()
    }

    override fun onDisable() {
        super.onDisable()
    }
}

object GrwSetting : AutoSavePluginConfig() {
    val token by value("unset")
}

object GrwData : AutoSavePluginData() {

}

object GrwCommand : CompositeCommand(
    Watcher, "grw",
    description = "GitHub Release Watcher"
) {
    @SubCommand
    suspend fun CommandSender.start() {

    }

    @SubCommand
    suspend fun CommandSender.stop() {

    }
}
