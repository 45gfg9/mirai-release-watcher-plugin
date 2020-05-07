package net.im45.bot.watcher;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.im45.bot.watcher.gh.Release;
import net.im45.bot.watcher.gh.RepoId;
import net.im45.bot.watcher.util.RepositoryException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Parser class for JSON payload GitHub returned
 *
 * @author 45gfg9
 */
public final class Parser {
    /**
     * The {@link SimpleDateFormat} object used to parse date format
     */
    private static final SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");

    /**
     * No instantiation.
     */
    private Parser() {
        throw new UnsupportedOperationException();
    }

    public static boolean hasErrors(JsonElement jsonElement) {
        return jsonElement.getAsJsonObject().has("errors");
    }

    public static JsonArray getErrors(JsonElement jsonElement) {
        return jsonElement.getAsJsonObject().getAsJsonArray("errors");
    }

    public static boolean hasData(JsonElement jsonElement) {
        return jsonElement.getAsJsonObject().has("data");
    }

    /**
     * Get repositories map from a big chunk of {@link JsonElement}.
     *
     * @param jsonElement data
     * @param repos Repositories watched
     * @return Map containing {@link RepoId} and corresponding {@link JsonObject}.
     */
    public static Map<RepoId, JsonObject> getRepositories(JsonElement jsonElement, Set<RepoId> repos) {
        Map<RepoId, JsonObject> map = new HashMap<>();
        JsonObject data = jsonElement.getAsJsonObject().getAsJsonObject("data");
        repos.forEach(s -> map.put(s, data.getAsJsonObject(Util.toLegalId(s))));
        return map;
    }

    // Optional is better than null
    public static Optional<Release> parseRelease(JsonElement jsonElement) throws RepositoryException {
        // TODO more precise error handling (using `error` object GitHub returned)
        if (jsonElement.isJsonNull()) {
            throw new RepositoryException("Repository is null (Probably because repository is not exist)");
        }

        JsonArray releaseNode = jsonElement.getAsJsonObject()
                .getAsJsonObject("releases")
                .getAsJsonArray("nodes");
        if (releaseNode.size() == 0) {
            return Optional.empty();
        }
        JsonObject release = releaseNode.get(0).getAsJsonObject();
        JsonObject author = release.getAsJsonObject("author");
        JsonObject assetsConn = release.getAsJsonObject("releaseAssets");
        int assetsCount = assetsConn.get("totalCount").getAsInt();
        JsonArray assets = assetsConn.get("nodes").getAsJsonArray();

        Release latest = new Release();
        latest.name = release.get("name").getAsString();
        latest.url = release.get("url").getAsString();
        latest.tagName = release.get("tagName").getAsString();
        try {
            latest.createdAt = fmt.parse(release.get("createdAt").getAsString());
            latest.publishedAt = fmt.parse(release.get("publishedAt").getAsString());
        } catch (ParseException e) {
            // This should never happen
            // If happened then either received data is corrupted,
            // or GitHub changed the format
            throw new RuntimeException(e);
        }
        latest.authorName = author.get("name").getAsString();
        latest.authorLogin = author.get("login").getAsString();
        latest.assets = new ArrayList<>(assetsCount);
        assets.forEach(e -> {
            JsonObject assetObj = e.getAsJsonObject();

            Release.Asset asset = new Release.Asset();
            asset.name = assetObj.get("name").getAsString();
            asset.size = assetObj.get("size").getAsLong();
            asset.downloadUrl = assetObj.get("downloadUrl").getAsString();

            latest.assets.add(asset);
        });

        return Optional.of(latest);
    }
}
