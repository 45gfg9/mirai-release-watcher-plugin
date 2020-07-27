package net.im45.bot.watcher;

import net.im45.bot.watcher.constant.Status;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.console.command.BlockingCommand;
import net.mamoe.mirai.console.command.CommandSender;
import net.mamoe.mirai.console.command.JCommandManager;
import net.mamoe.mirai.console.plugins.Config;
import net.mamoe.mirai.console.plugins.PluginBase;
import net.mamoe.mirai.console.scheduler.PluginScheduler;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.message.GroupMessageEvent;
import net.mamoe.mirai.utils.MiraiLogger;
import org.jetbrains.annotations.NotNull;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Consumer;

/**
 * Yet another <a href="https://github.com/mamoe/mirai-console">mirai-console</a> plugin.
 * <p>
 * This plugin uses <a href="https://developer.github.com/v4/">GitHub GraphQL API v4</a> to fetch data.
 * <p>
 * To use this plugin you need to generate a Personal Access Token on GitHub Developer settings page.
 * <p>
 * Only minimal permission is required (that is, PUBLIC_ACCESS)
 * <p>
 * Please note that this plugin only watches for {@code Releases}, not {@code Tags}.
 *
 * @author 45gfg9
 */
public class Watcher extends PluginBase {

    /**
     * The {@link Request} object.
     */
    private final Request request = new Request();

    /**
     * The logger.
     */
    private final MiraiLogger logger = getLogger();

    /**
     * Interval between each check in milliseconds.
     */
    private int intervalMs;

    /**
     * Config for plugin settings.
     */
    private Config settings;

    /**
     * Repeat task for checking new releases
     */
    private PluginScheduler.RepeatTaskReceipt repeatTask;

    /**
     * Used to transport data between {@link #onLoad()} and {@link #onEnable()}.
     */
    private Future<String> tempFuture;

    @Override
    public void onLoad() {
        super.onLoad();

        request.setErr(logger::error);
        request.setDebug(logger::debug);

        settings = loadConfig("settings.yml");
        settings.setIfAbsent("token", "Not set");
        settings.setIfAbsent("interval", 30 * 1000); // 30s default
        settings.setIfAbsent("timeout", 15 * 1000); // 15s default
        settings.setIfAbsent("autostart", false);

        String token = settings.getString("token");
        intervalMs = settings.getInt("interval");
        request.setTimeout(settings.getInt("timeout"));

        // Why scheduler is  `PluginScheduler?`?
        tempFuture = getScheduler().async(() -> request.setToken(token));

        // Load config for Request object
        request.load(loadConfig("watchers.yml"));
    }

    @Override
    public void onEnable() {
        super.onEnable();

        // Register plugin command
        JCommandManager.getInstance().register(this, new BlockingCommand(
                "grw",
                List.of(),
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
                    } else if (isNoTask()) {
                        startTask();
                        sender.sendMessageBlocking("Started running.");
                    } else {
                        sender.sendMessageBlocking("Already running!");
                    }

                } else if ("stop".equals(sub)) {
                    if (isNoTask()) {
                        sender.sendMessageBlocking("Not running!");
                    } else {
                        cancelTask();
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
                            sender.sendMessageBlocking("Interval set to " + arg + "ms");
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
                        Bot bot = Bot.getInstance(qq);
                        request.setConsumers(bot);
                        sender.sendMessageBlocking("Bot set.");
                    } else if ("timeout".equals(name)) {
                        try {
                            request.setTimeout(Integer.parseInt(arg));
                            sender.sendMessageBlocking("Timeout set to: " + arg + "ms");
                        } catch (NumberFormatException e) {
                            sender.sendMessageBlocking("Not a valid number: " + arg);
                        }
                    } else return false;
                } else if ("dump".equals(sub)) {
                    request.dump();

                    logger.debug("Interval: " + intervalMs);
                    logger.debug("RepeatTask: " + repeatTask);
                } else return false;

                return true;
            }
        });

        // Listen to group messages
        getEventListener().subscribeAlways(GroupMessageEvent.class, e -> {
            Group subject = e.getSubject();
            String msg = e.getMessage()
                    .toString()
                    .replaceFirst("\\[mirai:source:.*?]", "");
            if (msg.isEmpty()) return;
            List<String> msgs = new ArrayList<>(Arrays.asList(msg.split(" ")));
            msgs.removeIf(String::isBlank);

            String cmd = msgs.get(0);
            List<String> args = msgs.subList(1, msgs.size());

            if ("/watch-release".equals(cmd)) {
                int i = 0;
                for (String arg : args) {
                    if (request.add(arg, subject.getId(), subject::sendMessage)) {
                        i++;
                    }
                }
                if (i != 0) request.save();
                String s = "Added " + i + " repositor" + (i == 1 ? "y" : "ies") + ".";
                subject.sendMessage(s);
            } else if ("/unwatch-release".equals(cmd)) {
                int i = 0;
                for (String arg : args) {
                    if (request.remove(arg, subject.getId(), subject::sendMessage)) {
                        i++;
                    }
                }
                if (i != 0) request.save();
                String s = "Removed " + i + " repositor" + (i == 1 ? "y" : "ies") + ".";
                subject.sendMessage(s);
            } else if ("/watch-list".equals(cmd)) {
                StringWriter stringWriter = new StringWriter();
                PrintWriter printWriter = new PrintWriter(stringWriter);
                printWriter.println("Group " + subject.getId() + " is currently watching:");
                request.getWatched(subject.getId()).forEach((p) -> {
                    printWriter.print(p.first);
                    printWriter.print('/');
                    printWriter.print(p.second);
                    printWriter.println();
                });
                subject.sendMessage(stringWriter.toString().strip());
            }
        });

        // Retrieve token info
        String s;
        try {
            s = tempFuture.get();
            tempFuture = null; // don't use it again
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
        handleStatus(s, logger::info, logger::error);
        if (request.hasVerifiedToken() && settings.getBoolean("autostart")) {
            startTask();
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();

        // Unverified token will lost
        // Not bug, won't fix.
        settings.set("token", request.hasVerifiedToken() ? request.getToken() : "Not set");
        settings.set("interval", intervalMs);
        settings.set("timeout", request.getTimeout());
        settings.save();

        request.save();

        if (!isNoTask()) cancelTask();
    }

    private boolean isNoTask() {
        return repeatTask == null;
    }

    private void startTask() {
        repeatTask = getScheduler().repeat(request, intervalMs);
    }

    private void cancelTask() {
        repeatTask.setCancelled(true);
        repeatTask = null;
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
