package net.im45.bot.watcher.util;

import com.google.gson.JsonObject;
import net.im45.bot.watcher.Parser;
import net.im45.bot.watcher.gh.Release;
import net.im45.bot.watcher.gh.RepoId;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Util {
    private static final Pattern ID;
    private static final Pattern SSH;
    private static final Pattern HTTPS;

    static {
        ID = Pattern.compile("^([A-Za-z0-9-_]+)/([A-Za-z0-9-_]+)$");
        SSH = Pattern.compile("^git@github\\.com/([A-Za-z0-9-_]+)/([A-Za-z0-9-_]+)\\.git$");
        HTTPS = Pattern.compile("^https://github\\.com/([A-Za-z0-9-_]+)/([A-Za-z0-9-_]+)(?:\\.git)?$");
    }

    private Util() {
        throw new UnsupportedOperationException();
    }

    private static final char[] SUFFIXES = { 'K', 'M', 'G', 'T', 'P', 'E', 'Z', 'Y', 'B', 'N', 'D' };

    public static Map<RepoId, Pair<Release, Set<Long>>> filterNew(
            Map<RepoId, Pair<String, Set<Long>>> ver, Map<RepoId, JsonObject> repos) {

        Map<RepoId, Pair<Release, Set<Long>>> map = new HashMap<>();

        ver.forEach((n, v) -> {
            Release release = Parser.parseRelease(repos.get(n));
            if (!release.tagName.equals(v.first)) {
                ver.put(n, Pair.of(release.tagName, v.second));
                map.put(n, Pair.of(release, v.second));
            }
        });

        return map;
    }

    public static String byteScale(long bytes) {
        if (bytes < 0) {
            throw new IllegalArgumentException("Negative bytes");
        }
        int scale = (int) (Math.log10(bytes) / 3);
        if (scale > 0) return String.format("%.2f", bytes / Math.pow(1e3, scale)) + SUFFIXES[scale - 1] + "B";
        return bytes + "B";
    }

    public static Path getResource(Class<?> clazz, String resource) throws URISyntaxException, IOException {
        Path path;
        URI uri = clazz.getResource(resource).toURI();
        switch (uri.getScheme()) {
            case "file":
                path = Paths.get(uri);
                break;
            case "jar":
                path = FileSystems.newFileSystem(uri, Collections.emptyMap()).getPath(resource);
                break;
            default:
                throw new UnsupportedOperationException();
        }
        return path;
    }

    public static RepoId parseRepo(String url) {
        Matcher matcher;
        String owner;
        String name;

        if (!((matcher = ID.matcher(url)).find() ||
                (matcher = SSH.matcher(url)).find() ||
                (matcher = HTTPS.matcher(url)).find())) {
            throw new IllegalArgumentException();
        }

        owner = matcher.group(1);
        name = matcher.group(2);

        return RepoId.of(owner, name);
    }
}
