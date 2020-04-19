package net.im45.bot.watcher.gh;

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
}
