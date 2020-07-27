package net.im45.bot.watcher.gh;

import net.im45.bot.watcher.Parser;

import java.util.Date;
import java.util.List;

/**
 * Same as defined in {@code resources/frag.graphql}.
 * If any of that change please change this as well
 * and the {@link Parser} methods.
 *
 * @author 45gfg9
 */
public class Release {
    public String name;
    public String url;
    public String tagName;
    public Date createdAt;
    public Date publishedAt;
    public String authorName;
    public String authorLogin;
    public List<Asset> assets;

    public static class Asset {
        public String name;
        public long size;
        public String downloadUrl;
    }
}
