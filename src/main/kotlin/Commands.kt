import net.mamoe.mirai.console.command.*

object GrwCmd : CompositeCommand(
    Watcher, "grw",
    description = "GitHub Release Watcher"
) {
    @SubCommand
    suspend fun CommandSender.start() {
        sendMessage("Called start")
    }

    @SubCommand
    suspend fun CommandSender.stop() {
        sendMessage("Called stop")
    }

    // how to sub-sub-command
    @SubCommand
    suspend fun CommandSender.setToken(token: String) {
        GrwSettings.token = token
    }

    @SubCommand
    suspend fun CommandSender.setInterval(interval: Int) {
        GrwSettings.interval = interval
    }

    @SubCommand
    suspend fun CommandSender.setTimeout(timeout: Int) {
        GrwSettings.timeout = timeout
    }

    @SubCommand
    suspend fun CommandSender.setAutostart(autostart: Boolean) {
        GrwSettings.autostart = autostart
    }
}

object WatchReleaseCmd : SimpleCommand(
    Watcher, "watch-release",
    description = "Watch repositories"
) {
    @Handler
    suspend fun UserCommandSender.watch(args: String) {
    }
}

object UnwatchReleaseCmd : SimpleCommand(
    Watcher, "unwatch-release",
    description = "Unwatch repositories"
) {
    @Handler
    suspend fun UserCommandSender.unwatch(args: String) {
    }
}

object WatchListCmd : SimpleCommand(
    Watcher, "watch-list",
    description = "List watched repositories"
) {
    @Handler
    suspend fun UserCommandSender.list() {
    }
}
