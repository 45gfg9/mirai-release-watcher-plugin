package net.im45.bot.watcher;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.im45.bot.watcher.gh.Release;
import net.im45.bot.watcher.gh.RepoId;
import net.im45.bot.watcher.util.Pair;
import net.im45.bot.watcher.util.RepositoryException;
import org.jetbrains.annotations.Contract;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class.
 *
 * @author 45gfg9
 */
public class Util {

    /**
     * No instantiation.
     */
    private Util() {
        throw new UnsupportedOperationException();
    }

    private static final char[] SUFFIXES = { 'K', 'M', 'G', 'T', 'P', 'E', 'Z', 'Y', 'B', 'N', 'D' };

    /**
     * Filter out new repositories from a map whose values are {@link JsonObject}s.
     *
     * @param ver Map containing repositories' information
     * @param repos Map containing repositories' data.
     * @return filtered new repos.
     */
    public static Map<RepoId, Pair<Release, Set<Long>>> filterNew(
            Map<RepoId, Pair<String, Set<Long>>> ver,
            Map<RepoId, JsonElement> repos) {

        Map<RepoId, Pair<Release, Set<Long>>> map = new HashMap<>();

        Set<RepoId> nonexistent = new HashSet<>();

        ver.forEach((r, p) -> {
            Optional<Release> optRelease;
            try {
                optRelease = Parser.parseRelease(repos.get(r));
            } catch (RepositoryException e) {
                nonexistent.add(r);
                return;
            }
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

        // TODO implement notifications for nonexistent watches
        nonexistent.forEach(ver::remove);

        return map;
    }

    /**
     * Converts number of bytes to corresponding representations.
     * <p>
     * That is, {@code 3530} becomes {@code 3.53}KB and some such.
     * @param bytes number of bytes
     * @throws IllegalArgumentException if given number is negative
     * @return converted {@link String}
     */
    @Contract(pure = true)
    public static String byteScale(long bytes) {
        if (bytes < 0) {
            throw new IllegalArgumentException("Negative bytes");
        }
        int scale = (int) (Math.log10(bytes) / 3);
        if (scale == 0) return bytes + "B";
        return String.format("%.2f", bytes / Math.pow(1e3, scale)) + SUFFIXES[scale - 1] + "B";
    }

    /**
     * Get resource file path from either class file (development) or jar file (production).
     * @param clazz the (main (?)) class
     * @param resource the resource file name
     * @return resource file path
     * @throws URISyntaxException if something go wrong
     * @throws IOException same as above
     */
    @Contract(pure = true)
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

    /**
     * Converts a RepoId to a legal identifier.
     * @param repoId A {@link RepoId} object
     * @return The corresponding identifier
     */
    @Contract(pure = true)
    public static String toLegalId(RepoId repoId) {
        // No starting with numbers and no special characters.
        return repoId.toString()
                .replaceAll("^(\\d)", "_$1")
                .replaceAll("[-/.]", "_");
    }
}
