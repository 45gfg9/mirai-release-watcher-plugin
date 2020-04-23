package net.im45.bot.watcher;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.im45.bot.watcher.constants.Status;
import net.im45.bot.watcher.gh.Release;
import net.im45.bot.watcher.gh.RepoId;
import net.im45.bot.watcher.io.StringConsumerWriter;
import net.im45.bot.watcher.util.Pair;
import net.im45.bot.watcher.util.Util;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.console.plugins.Config;
import net.mamoe.mirai.console.plugins.ConfigSection;
import net.mamoe.mirai.console.plugins.ConfigSectionFactory;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

public class Request implements Runnable {

    private static final Pattern PATTERN = Pattern.compile("[0-9a-z]{40}");
    private static final URL ENDPOINT;
    private static final Path FRAGMENT_FILE_PATH;

    private final Map<RepoId, Pair<String, Set<Long>>> watch = new HashMap<>();
    private final Map<Long, Consumer<String>> groupOut = new HashMap<>();

    private Consumer<String> debug;
    private Consumer<String> err;

    private String token;
    private String tokenBuf;

    static {
        try {
            ENDPOINT = new URL("https://api.github.com/graphql");
            FRAGMENT_FILE_PATH = Util.getResource(Watcher.class, "/frag.graphql");
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public Request() {
        // Avoid NullPointerException
        this.err = this.debug = s -> {};
    }

    private static String getFragment() {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = Files.newBufferedReader(FRAGMENT_FILE_PATH)) {
            String s;
            while ((s = reader.readLine()) != null)
                sb.append(' ').append(s.strip());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return sb.toString().strip();
    }

    private static String buildQueryString(Set<RepoId> repos) {
        if (repos.size() == 0) throw new IllegalArgumentException();

        String fmt = "%s: repository(owner: \\\"%s\\\", name: \\\"%s\\\") { ...latestRelease } ";
        StringBuilder sb = new StringBuilder();

        sb.append("query {");
        for (RepoId repo : repos) {
            sb.append(String.format(fmt, Util.toLegalId(repo), repo.owner, repo.name));
        }
        sb.append("} ").append(getFragment());

        return String.format("{\"query\": \"%s\"}", sb.toString());
    }

    private static HttpsURLConnection newConnection(String token) throws IOException {
        HttpsURLConnection connection = (HttpsURLConnection) ENDPOINT.openConnection();
        connection.setRequestProperty("User-Agent", "45gfg9/16.33");
        connection.setRequestProperty("Accept-Encoding", "gzip");
        connection.setRequestProperty("Authorization", "bearer " + token);
        return connection;
    }

    private void writeOut(OutputStream out) throws IOException {
        String queryString = buildQueryString(watch.keySet());

        out.write(queryString.getBytes(StandardCharsets.UTF_8));
    }

    public void setDebug(Consumer<String> debug) {
        this.debug = debug;
    }

    public void setErr(Consumer<String> err) {
        this.err = err;
    }

    public boolean hasVerifiedToken() {
        return token != null;
    }

    public boolean hasUnverifiedToken() {
        return tokenBuf != null;
    }

    public String checkAgain() {
        if (hasUnverifiedToken()) return setToken(tokenBuf);
        return hasVerifiedToken() ? Status.OK : Status.INVALID_TOKEN;
    }

    public String getToken() {
        return token;
    }

    public String setToken(String token) {
        if (!PATTERN.matcher(token).matches()) {
            // Not a valid token (40 bytes of hexadecimal)
            return Status.INVALID_TOKEN;
        } else {
            this.tokenBuf = token;
            // Try connecting to GitHub
            try {
                newConnection(token).getInputStream().close();
            } catch (IOException e) {
                String msg = e.getMessage();
                if (msg.contains("401")) {
                    tokenBuf = null;
                    return Status.UNAUTHORIZED;
                }
                return e.getMessage();
            }
            this.tokenBuf = null;
            this.token = token;
            return Status.OK;
        }
    }

    public Set<RepoId> getWatched(long groupId) {
        Set<RepoId> set = new HashSet<>();

        watch.forEach((r, p) -> {
            if (p.second.contains(groupId)) {
                set.add(r);
            }
        });

        return set;
    }

    public void setConsumers(Bot bot) {
        watch.values()
                .stream()
                .flatMap(p -> p.second.stream())
                .distinct()
//                .mapToLong(Long::longValue) // I'm scared please help me
                .forEach(l -> groupOut.put(l, bot.getGroup(l)::sendMessage));
    }

    private Set<Long> getAllGroupIds() {
        return watch.values()
                .stream()
                .map(p -> p.second)
                .reduce(new HashSet<>(), (a, b) -> {
                    a.addAll(b);
                    return a;
                });
    }

    public boolean add(String repo, long groupId, Consumer<String> notify) {
        RepoId repoId;
        // TODO Verify repository existence
        try {
            repoId = Util.parseRepo(repo);
        } catch (IllegalArgumentException e) {
            notify.accept("Fuck you, what is " + repo + "?");
            return false;
        }
        return add(repoId, groupId, notify);
    }

    private boolean add(RepoId repo, long groupId, Consumer<String> notify) {
        Pair<String, Set<Long>> pair;
        if (watch.containsKey(repo)) {
            pair = watch.get(repo);
        } else {
            pair = Pair.of("?", new HashSet<>());
            watch.put(repo, pair);
        }

        Set<Long> set = pair.second;
        groupOut.put(groupId, notify);
        return set.add(groupId);
    }

    public boolean remove(String repo, long groupId) {
        RepoId repoId;
        try {
            repoId = Util.parseRepo(repo);
        } catch (IllegalArgumentException e) {
            return false;
        }
        return remove(repoId, groupId);
    }

    private boolean remove(RepoId repo, long groupId) {
        if (!watch.containsKey(repo)) return false;

        Set<Long> set = watch.get(repo).second;
        if (!set.contains(groupId)) return false;
        set.remove(groupId);
        if (!getAllGroupIds().contains(groupId)) {
            groupOut.remove(groupId);
        }

        if (set.size() == 0) watch.remove(repo);
        return true;
    }

    public void load(Config config) {
        config.asMap().keySet().forEach(s -> {
            ConfigSection c = config.getConfigSection(s);
            String[] id = s.split("/");
            String version = c.getString("version");
            List<Long> longs = c.getLongList("watcher");
            RepoId repoId = RepoId.of(id[0], id[1]);
            watch.put(repoId, Pair.of(version, new HashSet<>(longs)));
        });
        getAllGroupIds().forEach(l -> groupOut.put(l, s -> {}));
    }

    public void save(Config config) {
        watch.forEach((s, p) -> {
            ConfigSection section = ConfigSectionFactory.create();
            section.set("version", p.first);
            section.set("watcher", new ArrayList<>(p.second));
            config.set(s.toString(), section);
        });
        config.save();
    }

    @Override
    public void run() {
        if (watch.isEmpty()) return;

        JsonElement jsonElement;
        try {
            HttpsURLConnection connection = newConnection(token);
            connection.setDoOutput(true);

            OutputStream out = connection.getOutputStream();
            writeOut(out);

            InputStream in = connection.getInputStream();
            jsonElement = JsonParser.parseReader(new InputStreamReader(new GZIPInputStream(in)));
            in.close();
            connection.disconnect();
        } catch (IOException e) {
            err.accept(e.getMessage());
            return;
        }

        if (Parser.hasErrors(jsonElement)) {
            err.accept("Error received from upstream");
            JsonArray jsonArray = Parser.getErrors(jsonElement);
            debug.accept(jsonArray.toString());
            return;
        }
        if (!Parser.hasData(jsonElement)) {
            err.accept("Error! Received data doesn't have a \"data\" object?!");
            debug.accept(String.valueOf(jsonElement));
            return;
        }
        Map<RepoId, JsonObject> repos = Parser.getRepositories(jsonElement, watch.keySet());
        Map<RepoId, Pair<Release, Set<Long>>> newReleases = Util.filterNew(watch, repos);

        newReleases.forEach((n, p) -> {
            StringWriter stringWriter = new StringWriter();
            PrintWriter printWriter = new PrintWriter(stringWriter);
            Release r = p.first;

            printWriter.println("New release found for " + n + "!");
            printWriter.println("URL: " + r.url);
            printWriter.println("Name: " + r.name);
            printWriter.println("Tag Name: " + r.tagName);
            printWriter.println("Created At: " + r.createdAt);
            printWriter.println("Published At: " + r.publishedAt);
            printWriter.println("Author: " + r.authorLogin + " (" + r.authorName + ")");
            printWriter.println("This release has " + r.assets.size() + " asset(s).");
            for (Release.Asset a : r.assets) {
                printWriter.println("--------------------");
                printWriter.println("File name: " + a.name);
                printWriter.println("Size: " + Util.byteScale(a.size));
                printWriter.println("Download URL: " + a.downloadUrl);
            }

            for (long l : p.second) {
                groupOut.get(l).accept(stringWriter.toString().trim());
            }
        });
    }

    public void dump() {
        PrintWriter out = new PrintWriter(new StringConsumerWriter(debug));

        out.print("watch:");
        watch.forEach((r, p) -> {
            out.print(r);
            out.print(p.first);
            p.second.forEach(out::print);
        });

        out.print("");
        out.print("groupOut:");
        groupOut.forEach((l, c) -> {
            out.print(l);
            out.print(c);
        });

        out.print("token: " + token);
        out.print("tokenBuf: " + tokenBuf);
    }
}
