package net.im45.bot.watcher.gh;

import net.im45.bot.watcher.util.RepoIdFormatException;
import org.jetbrains.annotations.Contract;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class recording a Repository's ID.
 * <p>
 * If use Java 14, maybe consider use record?
 *
 * @author 45gfg9
 */
public final class RepoId {
    /**
     * Pattern for ID repository identifier.
     * <p>
     * Example, {@code 45gfg9/mirai-github-release-watcher}.
     */
    private static final Pattern ID;

    /**
     * Pattern for SSH clone link.
     * <p>
     * Example, {@code git@github.com:45gfg9/mirai-release-watcher-plugin.git}
     */
    private static final Pattern SSH;

    /**
     * Pattern for both Repository URL and HTTPS clone link.
     * <p>
     * Example, {@code https://github.com/45gfg9/mirai-release-watcher-plugin} <br>
     * or {@code https://github.com/45gfg9/mirai-release-watcher-plugin.git}
     */
    private static final Pattern HTTPS;

    // line will be too long to join declaration and assignment
    static {
        ID = Pattern.compile("^([A-Za-z0-9-_]+)/([A-Za-z0-9-_]+)$");
        SSH = Pattern.compile("^git@github\\.com:([A-Za-z0-9-_]+)/([A-Za-z0-9-_]+)\\.git$");
        HTTPS = Pattern.compile("^https://github\\.com/([A-Za-z0-9-_]+)/([A-Za-z0-9-_]+)(?:\\.git)?$");
    }

    public final String owner;
    public final String name;

    /**
     * Private constructor, please use {@link #of(String, String)}
     *
     * @param owner Repository owner
     * @param name Repository name
     */
    private RepoId(String owner, String name) {
        this.owner = owner;
        this.name = name;
    }

    /**
     * Factory method for creating {@link RepoId} objects.
     *
     * @param owner Repository owner
     * @param name Repository name
     * @return a {@link RepoId} object
     */
    @Contract(pure = true)
    public static RepoId of(String owner, String name) {
        return new RepoId(owner, name);
    }

    /**
     * Parse a RepoId from a given url.
     *
     * @param url The given url.
     * @throws RepoIdFormatException if given url is not accepted
     * @return A representing {@link RepoId} object.
     */
    @Contract(pure = true)
    public static RepoId parse(String url) {
        Matcher matcher;
        String owner;
        String name;

        if (!((matcher = ID.matcher(url)).find() ||
                (matcher = SSH.matcher(url)).find() ||
                (matcher = HTTPS.matcher(url)).find())) {
            // Only these three formats are accepted
            // If not, throw an exception
            throw RepoIdFormatException.forInputString(url);
        }

        owner = matcher.group(1);
        name = matcher.group(2);

        return of(owner, name);
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    @Contract(pure = true)
    public String toString() {
        return owner + "/" + name;
    }

    /**
     * {@inheritDoc}
     *
     * @param o {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        // final class
        if (!(o instanceof RepoId)) return false;
        RepoId repoId = (RepoId) o;
        return owner.equals(repoId.owner) &&
                name.equals(repoId.name);
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(owner, name);
    }
}

