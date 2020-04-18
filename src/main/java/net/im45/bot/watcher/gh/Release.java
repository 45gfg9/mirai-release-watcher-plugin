package net.im45.bot.watcher.gh;

import java.util.Date;
import java.util.List;

public class Release {
    public String name;
    public String url;
    public String tagName;
    public Date createdAt;
    public Date publishedAt;
    public String authorName;
    public String authorLogin;
    public List<Asset> assets;
}
