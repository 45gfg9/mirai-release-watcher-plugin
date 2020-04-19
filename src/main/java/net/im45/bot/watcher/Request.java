package net.im45.bot.watcher;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.im45.bot.watcher.gh.Asset;
import net.im45.bot.watcher.gh.Release;
import net.im45.bot.watcher.gh.RepoId;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.console.plugins.Config;

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
            String s = reader.readLine();
            sb.append(' ').append(s.strip());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return sb.toString().strip();
    }

    private static String buildQueryString(Set<RepoId> repos) {
        if (repos.size() == 0) throw new IllegalArgumentException();

        String fmt = "%s: repository(owner: \"%s\", name: \"%s\") { ...latestRelease } ";
        StringBuilder sb = new StringBuilder();

        sb.append("query {");
        for (RepoId repo : repos) {
            sb.append(String.format(fmt, repo.owner, repo.name));
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

    private static void writeOut(OutputStream out, Set<RepoId> repos) throws IOException {
        String queryString = buildQueryString(repos);

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

    public String getToken() {
        return token;
    }

    public String checkAgain() {
        if (hasUnverifiedToken()) return setToken(tokenBuf);
        return hasVerifiedToken() ? Status.OK : Status.INVALID_TOKEN;
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

    public void setConsumers(Bot bot) {
        watch.values()
                .stream()
                .flatMap(p -> p.second.stream())
                .mapToLong(Long::longValue)
                .forEach(l -> groupOut.put(l, bot.getGroup(l)::sendMessage));
    }

    public boolean add(String repo, long groupId, Consumer<String> notify) {
        RepoId repoId;
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
            pair = Pair.of("_", new HashSet<>());
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
            // TODO
            return false;
        }
        return remove(repoId, groupId);
    }

    private boolean remove(RepoId repo, long groupId) {
        if (!watch.containsKey(repo)) return false;

        Set<Long> list = watch.get(repo).second;
        if (!list.contains(groupId)) return false;
        list.remove(groupId);
        groupOut.remove(groupId);

        if (list.size() == 0) watch.remove(repo);
        return true;
    }

    public void load(Config config) {
        config.asMap().keySet().forEach((s) -> {
            String[] split = s.split("__ver__");
            String[] info = split[0].split("/");
            RepoId n = RepoId.of(info);
            List<Long> longList = config.getLongList(s);
            watch.put(n, Pair.of(split[1], new HashSet<>(longList)));
        });
    }

    public void save(Config config) {
        watch.forEach((s, p) -> {
            String n = s + "__ver__" + p.first;
            config.set(n, new ArrayList<>(p.second));
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
            writeOut(out, watch.keySet());
            out.close();

            InputStream in = connection.getInputStream();
            jsonElement = JsonParser.parseReader(new InputStreamReader(in));
            in.close();
        } catch (IOException e) {
            err.accept(e.getMessage());
            return;
        }

        if (Parser.hasErrors(jsonElement)) {
            err.accept("Error received from upstream");
            JsonArray jsonArray = Parser.getErrors(jsonElement);
//            jsonArray.forEach(e -> err.accept(e.getAsJsonObject().get("message").getAsString()));
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
            printWriter.println("Author: " + r.authorLogin + "(" + r.authorName + ")");
            printWriter.println("This release has " + r.assets.size() + " assets.");
            for (Asset a : r.assets) {
                printWriter.println("----------");
                printWriter.println("File name: " + a.name);
                printWriter.println("Size: " + Util.byteScale(a.size));
                printWriter.println("Download URL: " + a.downloadUrl);
            }

            for (long l : p.second) {
                groupOut.get(l).accept(stringWriter.toString());
            }
        });
    }
}
