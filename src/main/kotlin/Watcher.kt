import com.google.auto.service.AutoService
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.unregisterAllCommands
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

        GrwSettings.reload()
        GrwWatches.reload()

        GrwCmd.register()
        WatchReleaseCmd.register()
        UnwatchReleaseCmd.register()
        WatchListCmd.register()
    }

    override fun onDisable() {
        super.onDisable()

        unregisterAllCommands()
    }
}
