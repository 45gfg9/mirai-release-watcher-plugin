package net.im45.bot.grw

import net.im45.bot.grw.github.RepoId
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.CompositeCommand
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.console.command.UserCommandSender

object GrwCmd : CompositeCommand(
    Watcher, "grw",
    description = "GitHub Release Watcher"
) {
    private val TOKEN_REGEX = Regex("^[0-9a-fA-F]{40}$")

    @SubCommand
    suspend fun CommandSender.enable() {
        GrwSettings.enabled = true
        sendMessage("GitHub Release Watcher enabled")
    }

    @SubCommand
    suspend fun CommandSender.disable() {
        GrwSettings.enabled = false
        sendMessage("GitHub Release Watcher disabled")
    }

    // sub-sub-command (grass
    // https://github.com/mamoe/mirai-console/issues/237
    @SubCommand("set token")
    suspend fun CommandSender.setToken(token: String) {
        if (!(TOKEN_REGEX matches token)) {
            sendMessage("Wrong format token")
            return
        }
        if (Request.verifyToken(token)) {
            sendMessage("Token set")
        } else {
            sendMessage("Invalid token")
        }
    }

    @SubCommand("set interval")
    suspend fun CommandSender.setInterval(interval: Long) {
        GrwSettings.interval = interval
        sendMessage("Set interval to ${interval}s")
    }

    @SubCommand("set timeout")
    suspend fun CommandSender.setTimeout(timeout: Long) {
        GrwSettings.timeout = timeout
        sendMessage("Set timeout to ${timeout}ms")
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
            runCatching {
                val repo = RepoId.parse(arg)
                GrwWatches.watches[repo] = "?" to setOf(subject.id)
                cnt++
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
            runCatching {
                val repo = RepoId.parse(arg)
                if (GrwWatches.watches.remove(repo) != null) cnt++
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
