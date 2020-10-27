package net.im45.bot.grw

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.CompositeCommand
import net.mamoe.mirai.console.command.SimpleCommand

object GrwCmd : CompositeCommand(
    Watcher, "grw",
    description = "GitHub Release Watcher"
) {
    @SubCommand
    suspend fun CommandSender.start() {
        if (GrwData.grwJob == null) {
            GrwData.grwJob = GlobalScope.launch(Dispatchers.IO) {
                while (true) {
                    delay(GrwSettings.interval)
                    Request.request()
                }
            }
            sendMessage("Started running.")
        } else {
            sendMessage("Already running!")
        }
    }

    @SubCommand
    suspend fun CommandSender.stop() {
        if (GrwData.grwJob == null) {
            sendMessage("Not running!")
        } else {
            GrwData.grwJob!!.cancel()
            GrwData.grwJob = null
            sendMessage("Stopped running.")
        }
    }

    // how to sub-sub-command
    @SubCommand
    suspend fun CommandSender.setToken(token: String) {
        GrwSettings.token = token
        TODO("Verify token")
    }

    @SubCommand
    suspend fun CommandSender.setInterval(interval: Long) {
        GrwSettings.interval = interval
    }

    @SubCommand
    suspend fun CommandSender.setTimeout(timeout: Long) {
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
    suspend fun CommandSender.watch(args: String) {
        TODO()
    }
}

object UnwatchReleaseCmd : SimpleCommand(
    Watcher, "unwatch-release",
    description = "Unwatch repositories"
) {
    @Handler
    suspend fun CommandSender.unwatch(args: String) {
        TODO()
    }
}

object WatchListCmd : SimpleCommand(
    Watcher, "watch-list",
    description = "List watched repositories"
) {
    @Handler
    suspend fun CommandSender.list() {
        TODO()
    }
}