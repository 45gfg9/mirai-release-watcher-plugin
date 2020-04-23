package net.im45.bot.watcher;

import net.im45.bot.watcher.constants.Status;
import net.im45.bot.watcher.gh.RepoId;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.console.command.BlockingCommand;
import net.mamoe.mirai.console.command.CommandSender;
import net.mamoe.mirai.console.command.JCommandManager;
import net.mamoe.mirai.console.plugins.Config;
import net.mamoe.mirai.console.plugins.PluginBase;
import net.mamoe.mirai.console.scheduler.PluginScheduler;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.message.GroupMessage;
import net.mamoe.mirai.utils.MiraiLogger;
import org.jetbrains.annotations.NotNull;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Consumer;

/**
 * Yet another <a href="https://github.com/mamoe/mirai-console">mirai-console</a> plugin.
 * <p>
 * Currently WIP, not functional.
 * <p>
 * This plugin uses <a href="https://developer.github.com/v4/">GitHub GraphQL API v4</a> to fetch data.
 * <p>
 * To use this plugin you need to generate a Personal Access Token on GitHub Developer settings page.
 * <p>
 * Only minimal permission is required (that is, PUBLIC_ACCESS)
 */

public class Watcher extends PluginBase {

    private static final Request request = new Request();
    private final MiraiLogger logger = getLogger();

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

        request.setErr(logger::error);
        request.setDebug(logger::debug);

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
                "grw",
                Collections.emptyList(),
                "GitHub Release Watcher",
                "/grw <start|stop|set <token|interval|autostart|bot> <arg>>"
        ) {
            @Override
            public boolean onCommandBlocking(@NotNull CommandSender sender, @NotNull List<String> args) {
                Iterator<String> it = args.iterator();
                if (!it.hasNext()) return false;
                String sub = it.next();

                if ("start".equals(sub)) {
                    if (request.hasUnverifiedToken()) {
                        handleStatus(request.checkAgain(), sender::sendMessageBlocking);
                    }
                    if (!request.hasVerifiedToken() && !request.hasUnverifiedToken()) {
                        sender.sendMessageBlocking("You don't have a valid Access Token. " +
                                "Please set by /github set token <Token>");
                    } else if (request.hasUnverifiedToken()) {
                        sender.sendMessageBlocking("Token validation failed. " +
                                "Please try again later.");
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
                } else if ("dump".equals(sub)) {
                    logger.debug("Interval: " + intervalMs);
                    logger.debug("RepeatTask: " + repeatTask);

                    request.dump();
                } else return false;

                return true;
            }
        });

        getEventListener().subscribeAlways(GroupMessage.class, e -> {
            Group subject = e.getSubject();
            List<String> msg = new ArrayList<>(Arrays.asList(e.getMessage()
                    .toString()
                    .replaceFirst("\\[mirai:source:.*?]", "")
                    .split(" ")));
            msg.removeIf(String::isBlank);
            if (msg.size() == 0) return;

            String cmd = msg.get(0);
            List<String> args = msg.subList(1, msg.size());

            if ("/watch-release".equals(cmd)) {
                int adds = 0;
                for (String arg : args) {
                    if (request.add(arg, subject.getId(), subject::sendMessage)) {
                        adds++;
                    }
                }
                String s = "Added " + adds + " repositor" + (adds == 1 ? "y" : "ies") + ".";
                subject.sendMessage(s);
            } else if ("/unwatch-release".equals(cmd)) {
                int i = 0;
                for (String arg : args) {
                    if (request.remove(arg, subject.getId())) {
                        i++;
                    }
                }
                String s = "Removed " + i + " repositor" + (i == 1 ? "y" : "ies") + ".";
                subject.sendMessage(s);
            } else if ("/watch-list".equals(cmd)) {
                StringWriter stringWriter = new StringWriter();
                PrintWriter printWriter = new PrintWriter(stringWriter);
                printWriter.println("Group " + subject.getId() + " is currently watching:");
                for (RepoId repo : request.getWatched(subject.getId())) {
                    printWriter.println(repo);
                }
                subject.sendMessage(stringWriter.toString().trim());
            }
        });

        String s;
        try {
            s = future.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
        handleStatus(s, logger::info, logger::error);
        if (request.hasVerifiedToken() && settings.getBoolean("autostart")) {
            repeatTask = getScheduler().repeat(request, intervalMs);
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();

        settings.set("token", request.hasVerifiedToken() ? request.getToken() : "Not set");
        settings.set("interval", intervalMs);
        // TODO Add a `defaultBot` config
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
