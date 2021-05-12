package net.im45.bot.grw

import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.unregisterAllCommands
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription
import net.mamoe.mirai.console.plugin.jvm.KotlinPlugin

object Watcher : KotlinPlugin(
    JvmPluginDescription(
        "net.im45.bot.grw",
        "1.0.0-dev01",
        "GitHub Release Watcher",
    ) {
        author("45gfg9")
    }
) {
    override fun onEnable() {
        super.onEnable()

        GrwSettings.reload()
        GrwWatches.reload()

        Request.tokenBuf = GrwSettings.token
        GrwSettings.token = "unset"

        Request.init()

        GrwCmd.register()
        WatchReleaseCmd.register()
        UnwatchReleaseCmd.register()
        WatchListCmd.register()
    }

    override fun onDisable() {
        super.onDisable()

        Request.close()
        unregisterAllCommands(this)
    }
}
