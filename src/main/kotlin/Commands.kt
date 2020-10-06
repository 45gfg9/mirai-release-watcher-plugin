import net.mamoe.mirai.console.command.*

object GrwCmd : CompositeCommand(
    Watcher, "grw",
    description = "GitHub Release Watcher"
) {
    @SubCommand
    suspend fun CommandSender.start() {

    }

    @SubCommand
    suspend fun CommandSender.stop() {

    }

    @SubCommand
    suspend fun CommandSender.setToken(token: String) {
        GrwSetting.token = token
    }

    @SubCommand
    suspend fun CommandSender.setInterval(interval: Int) {
        GrwSetting.interval = interval
    }

    @SubCommand
    suspend fun CommandSender.setTimeout(timeout: Int) {
        GrwSetting.timeout = timeout
    }

    @SubCommand
    suspend fun CommandSender.setAutostart(autostart: Boolean) {
        GrwSetting.autostart = autostart
    }
}

object WatchReleaseCmd : SimpleCommand(
    Watcher, "watch-release",
    description = "Watch repositories"
) {
    @Handler
    suspend fun UserCommandSender.handle(vararg args: String) {
    }
}

object UnwatchReleaseCmd : SimpleCommand(
    Watcher, "unwatch-release",
    description = "Unwatch repositories"
) {
    @Handler
    suspend fun UserCommandSender.handle(vararg args: String) {
    }
}

object WatchListCmd : SimpleCommand(
    Watcher, "watch-list",
    description = "List watched repositories"
) {
    @Handler
    suspend fun UserCommandSender.handle() {
    }
}
