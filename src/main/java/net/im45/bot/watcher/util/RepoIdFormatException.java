package net.im45.bot.watcher.util;

import net.im45.bot.watcher.gh.RepoId;

/**
 * Thrown to indicate that the application has attempted to convert
 * a string to a {@link RepoId}, but that the string does not
 * have the appropriate format.
 *
 * @see RepoId#parse(String)
 * @author 45gfg9
 */
public class RepoIdFormatException extends IllegalArgumentException {

    /**
     * Constructs an {@code RepoIdFormatException} with no detail message.
     */
    public RepoIdFormatException() {
        super();
    }

    /**
     * Constructs a {@code RepoIdFormatException} with the
     * specified detail message.
     *
     * @param s the detail message.
     */
    public RepoIdFormatException(String s) {
        super(s);
    }

    /**
     * Factory method for making a {@code RepoIdFormatException}
     * given the specified input which caused the error.
     *
     * @param s the input causing the error
     */
    public static RepoIdFormatException forInputString(String s) {
        return new RepoIdFormatException("For input string: \"" + s + "\"");
    }
}
