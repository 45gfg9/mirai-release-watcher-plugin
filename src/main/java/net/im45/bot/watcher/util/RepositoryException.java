package net.im45.bot.watcher.util;

/**
 * Thrown to indicate a exception related to a Repository.
 *
 * @author 45gfg9
 */
public class RepositoryException extends Exception {
    /**
     * Constructs a {@code RepositoryException} with no
     * detail message.
     */
    public RepositoryException() {
        super();
    }

    /**
     * Constructs a {@code RepositoryException} with the
     * specified detail message.
     *
     * @param s the detail message.
     */
    public RepositoryException(String s) {
        super(s);
    }
}
