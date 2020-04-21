package net.im45.bot.watcher;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.im45.bot.watcher.gh.Release;
import net.im45.bot.watcher.gh.RepoId;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public final class Parser {
    private static final SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");

    private Parser() {
        throw new UnsupportedOperationException();
    }

    public static boolean hasErrors(JsonElement jsonElement) {
        return jsonElement.getAsJsonObject().has("errors");
    }

    public static boolean hasData(JsonElement jsonElement) {
        return jsonElement.getAsJsonObject().has("data");
    }

    public static JsonArray getErrors(JsonElement jsonElement) {
        return jsonElement.getAsJsonObject().getAsJsonArray("errors");
    }

    public static Map<RepoId, JsonObject> getRepositories(JsonElement jsonElement, Collection<RepoId> repos) {
        Map<RepoId, JsonObject> map = new HashMap<>();
        JsonObject data = jsonElement.getAsJsonObject().getAsJsonObject("data");
        repos.forEach(s -> map.put(s, data.getAsJsonObject(s.owner + "__sep__" + s.name)));
        return map;
    }

    public static Release parseRelease(JsonElement jsonElement) {
        JsonObject release = jsonElement.getAsJsonObject()
                .getAsJsonObject("releases")
                .getAsJsonArray("nodes")
                .get(0)
                .getAsJsonObject();
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

        return latest;
    }
}
