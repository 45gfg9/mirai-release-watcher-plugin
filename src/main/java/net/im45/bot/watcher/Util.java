package net.im45.bot.watcher;

import com.google.gson.JsonObject;
import net.im45.bot.watcher.gh.Release;
import net.im45.bot.watcher.gh.RepoId;
import net.im45.bot.watcher.util.Pair;

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
        SSH = Pattern.compile("^git@github\\.com:([A-Za-z0-9-_]+)/([A-Za-z0-9-_]+)\\.git$");
        HTTPS = Pattern.compile("^https://github\\.com/([A-Za-z0-9-_]+)/([A-Za-z0-9-_]+)(?:\\.git)?$");
    }

    private Util() {
        throw new UnsupportedOperationException();
    }

    private static final char[] SUFFIXES = { 'K', 'M', 'G', 'T', 'P', 'E', 'Z', 'Y', 'B', 'N', 'D' };

    public static Map<RepoId, Pair<Release, Set<Long>>> filterNew(
            Map<RepoId, Pair<String, Set<Long>>> ver,
            Map<RepoId, JsonObject> repos) {

        Map<RepoId, Pair<Release, Set<Long>>> map = new HashMap<>();

        ver.forEach((r, p) -> {
            Optional<Release> optRelease = Parser.parseRelease(repos.get(r));
            if (optRelease.isEmpty()) {
                ver.put(r, Pair.of("-", p.second));
                return;
            }

            Release release = optRelease.get();
            if (!release.tagName.equals(p.first)) {
                ver.put(r, Pair.of(release.tagName, p.second));
                map.put(r, Pair.of(release, p.second));
            }
        });

        return map;
    }

    public static String byteScale(long bytes) {
        if (bytes < 0) {
            throw new IllegalArgumentException("Negative bytes");
        }
        int scale = (int) (Math.log10(bytes) / 3);
        if (scale == 0) return bytes + "B";
        return String.format("%.2f", bytes / Math.pow(1e3, scale)) + SUFFIXES[scale - 1] + "B";
    }

    public static Path getResource(Class<?> clazz, String resource) throws URISyntaxException, IOException {
        URI uri = clazz.getResource(resource).toURI();
        String scheme = uri.getScheme();
        switch (scheme) {
            case "file":
                return Paths.get(uri);
            case "jar":
                return FileSystems.newFileSystem(uri, Collections.emptyMap()).getPath(resource);
            default:
                throw new IllegalStateException("Unknown scheme: " + scheme);
        }
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

    public static String toLegalId(RepoId repoId) {
        return repoId.toString()
                .replaceAll("^\\d+", "")
                .replaceAll("[-/.]", "_");
    }
}
