package net.im45.bot.watcher.util;

public class RepoIdFormatException extends IllegalArgumentException {
    public RepoIdFormatException() {
        super();
    }

    public RepoIdFormatException(String s) {
        super(s);
    }

    public static RepoIdFormatException forInputString(String s) {
        return new RepoIdFormatException("For input string: \"" + s + "\"");
    }
}
