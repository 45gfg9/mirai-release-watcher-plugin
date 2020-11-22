package net.im45.bot.grw

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.im45.bot.grw.github.RepoId
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.CompositeCommand
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.console.command.UserCommandSender

object GrwCmd : CompositeCommand(
        Watcher, "grw",
        description = "GitHub Release Watcher"
) {
    private val TOKEN_REGEX = Regex("[0-9a-fA-F]{40}")

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
    suspend fun CommandSender.setBot(id: Long) {
        if (id != 0L && Bot.getInstanceOrNull(id) == null) {
            sendMessage("Bot not found")
            return
        }
        GrwSettings.botId = id
        sendMessage("Bot set")
    }

    @SubCommand
    suspend fun CommandSender.setToken(token: String) {
        if (!TOKEN_REGEX.matches(token)) {
            sendMessage("Wrong format token")
            return
        }
        if (Request.verifyToken(token)) {
            sendMessage("Token set")
        } else {
            sendMessage("Invalid token")
        }
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
    suspend fun UserCommandSender.watch(vararg args: String) {
        var cnt = 0
        for (arg in args)
            try {
                val repo = RepoId.parse(arg)
                GrwWatches.watches[repo] = "?" to setOf(subject.id)
                cnt++
            } catch (ignored: RepoIdFormatException) {
            }
        sendMessage("Added $cnt repositor${if (cnt == 1) "y" else "ies"}")
    }
}

object UnwatchReleaseCmd : SimpleCommand(
        Watcher, "unwatch-release",
        description = "Unwatch repositories"
) {
    @Handler
    suspend fun UserCommandSender.unwatch(vararg args: String) {
        var cnt = 0
        for (arg in args)
            try {
                val repo = RepoId.parse(arg)
                if (GrwWatches.watches.remove(repo) != null) cnt++
            } catch (ignored: RepoIdFormatException) {
            }
        sendMessage("Removed $cnt repositor${if (cnt == 1) "y" else "ies"}")
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
