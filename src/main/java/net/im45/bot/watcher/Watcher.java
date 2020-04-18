package net.im45.bot.watcher;

import net.mamoe.mirai.Bot;
import net.mamoe.mirai.console.command.BlockingCommand;
import net.mamoe.mirai.console.command.CommandSender;
import net.mamoe.mirai.console.command.JCommandManager;
import net.mamoe.mirai.console.plugins.Config;
import net.mamoe.mirai.console.plugins.PluginBase;
import net.mamoe.mirai.console.scheduler.PluginScheduler;
import net.mamoe.mirai.message.GroupMessage;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Consumer;

/**
 * This plugin uses
 * <a href="https://developer.github.com/v4/">GitHub GraphQL API v4</a>
 * to fetch data.
 * <p>
 * To use this plugin you need to generate a Personal Access Token
 * on GitHub Developer settings page.
 * <p>
 * Only minimal permission is required (that is, PUBLIC_ACCESS)
 */

//@SuppressWarnings("unused")
public class Watcher extends PluginBase {

    private final static Request request = new Request();

    // Interval between checks
    private int intervalMs;

    private Bot bot;

    private Config settings;
    private Config watchers;

    private PluginScheduler.RepeatTaskReceipt repeatTask;

    private Future<String> future;

    @Override
    public void onLoad() {
        super.onLoad();

        request.setErr(getLogger()::error);
        request.setDebug(getLogger()::debug);

        settings = loadConfig("settings.yml");
        settings.setIfAbsent("token", "Not set");
        settings.setIfAbsent("interval", 60 * 1000);
        settings.setIfAbsent("autostart", false);

        String token = settings.getString("token");
        intervalMs = settings.getInt("interval");

        future = getScheduler().async(() -> request.setToken(token));

        watchers = loadConfig("watchers.yml");
        request.load(watchers);
    }

    @Override
    public void onEnable() {
        super.onEnable();

        JCommandManager.getInstance().register(this, new BlockingCommand(
                "github",
                List.of("gh"),
                "GitHub Release Watcher",
                "/github <start|stop|set <token|interval|autostart|bot> <arg>>"
        ) {
            @Override
            public boolean onCommandBlocking(@NotNull CommandSender sender, @NotNull List<String> args) {
                Iterator<String> it = args.iterator();
                if (!it.hasNext()) return false;
                String sub = it.next();

                request.add("ppy/osu", 0L, s->{});

                if ("start".equals(sub)) {
                    if (request.hasUnverifiedToken()) {
                        handleStatus(request.checkAgain(), sender::sendMessageBlocking);
                    }
                    if (!request.hasVerifiedToken() && !request.hasUnverifiedToken()) {
                        sender.sendMessageBlocking("You don't have a valid Access Token. " +
                                "Please set by /github set token <Token>");
                    } else if (repeatTask == null) {
                        repeatTask = getScheduler().repeat(request, intervalMs);
                        sender.sendMessageBlocking("Started running.");
                    } else {
                        sender.sendMessageBlocking("Already running!");
                    }

                } else if ("stop".equals(sub)) {
                    if (repeatTask == null) {
                        sender.sendMessageBlocking("Not running!");
                    } else {
                        repeatTask.setCancelled(true);
                        repeatTask = null;
                        sender.sendMessageBlocking("Stopped running.");
                    }

                } else if ("set".equals(sub)) {
                    if (!it.hasNext()) return false;
                    String name = it.next();
                    if (!it.hasNext()) return false;
                    String arg = it.next();

                    if ("token".equals(name)) {
                        String token = arg.toLowerCase();
                        handleStatus(request.setToken(token), sender::sendMessageBlocking);
                    } else if ("interval".equals(name)) {
                        try {
                            intervalMs = Integer.parseInt(arg);
                            sender.sendMessageBlocking("Interval set to " + arg);
                        } catch (NumberFormatException e) {
                            sender.sendMessageBlocking("Not a valid number: " + arg);
                        }
                    } else if ("autostart".equals(name)) {
                        boolean start = Boolean.parseBoolean(arg);
                        settings.set("autostart", start);
                        sender.sendMessageBlocking("Autostart set to: " + start);
                    } else if ("bot".equals(name)) {
                        long qq;
                        try {
                            qq = Long.parseLong(arg);
                        } catch (NumberFormatException e) {
                            sender.sendMessageBlocking("Not a valid number: " + arg);
                            return true;
                        }
                        bot = Bot.getInstance(qq);
                        request.setConsumers(bot);
                        sender.sendMessageBlocking("Bot set.");
                    } else return false;
                } else return false;

                return true;
            }
        });

        getEventListener().subscribeAlways(GroupMessage.class, e -> {
            if (e.getGroup().getId() == 617745343L) return;
            List<String> msg = Arrays.asList(e.getMessage().toString().replaceFirst("\\[mirai:source:\\d+]", "")
                    .split(" "));
            msg.removeIf(String::isBlank);
            if (msg.size() == 0) return;

            String cmd = msg.get(0);
            List<String> args = msg.subList(1, msg.size());

            if ("/watch-release".equals(cmd)) {
                for (String arg : args) {
                    // TODO
                }
            }
        });

        String s;
        try {
            s = future.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
        handleStatus(s, getLogger()::info, getLogger()::error);
        if (request.hasVerifiedToken() && settings.getBoolean("autostart")) {
            repeatTask = getScheduler().repeat(request, intervalMs);
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();

        settings.set("token", request.hasVerifiedToken() ? request.getToken() : "Not set");
        settings.set("interval", intervalMs);
        settings.save();

        request.save(watchers);
    }

    public void handleStatus(String s, Consumer<String> out) {
        handleStatus(s, out, out);
    }

    public void handleStatus(String s, Consumer<String> out, Consumer<String> err) {
        if (Status.OK.equals(s)) {
            out.accept("Token validated.");
        } else if (Status.UNAUTHORIZED.equals(s) || Status.INVALID_TOKEN.equals(s)) {
            if (!request.hasVerifiedToken()) {
                err.accept("Error: invalid access token (or not set). " +
                        "Please set your access key by /github set token <Token>");
            } else {
                err.accept("Invalid access token.");
            }
        } else {
            err.accept("IOException occurred: " + s);
        }
    }
}
