package net.im45.bot.watcher.gh;

import java.util.Objects;

/**
 * Utility class recording a Repository's ID.
 *
 * If use Java 14, maybe consider use record.
 *
 * @author 45gfg9
 */
public final class RepoId {
    public final String owner;
    public final String name;

    private RepoId(String owner, String name) {
        this.owner = owner;
        this.name = name;
    }

    public static RepoId of(String owner, String name) {
        return new RepoId(owner, name);
    }

    @Override
    public String toString() {
        return owner + "/" + name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RepoId)) return false;
        RepoId repoId = (RepoId) o;
        return owner.equals(repoId.owner) &&
                name.equals(repoId.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(owner, name);
    }
}

