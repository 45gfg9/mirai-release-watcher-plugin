package net.im45.bot.watcher.gh;

import java.util.Objects;

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

    public static RepoId of(String[] repo) {
        if (repo.length != 2) {
            throw new IllegalArgumentException();
        }

        return of(repo[0], repo[1]);
    }

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
