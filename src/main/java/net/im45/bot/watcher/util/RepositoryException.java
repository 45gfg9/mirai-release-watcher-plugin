package net.im45.bot.watcher.util;

/**
 * Indicates a exception related to a Repository.
 *
 * @author 45gfg9
 */
public class RepositoryException extends Exception {
    /**
     * Constructs a new exception with {@code null} as its detail message.
     * The cause is not initialized, and may subsequently be initialized by a
     * call to {@link #initCause}.
     */
    public RepositoryException() {
        super();
    }

    /**
     * Constructs a new exception with the specified detail message.  The
     * cause is not initialized, and may subsequently be initialized by
     * a call to {@link #initCause}.
     *
     * @param message the detail message. The detail message is saved for
     *                later retrieval by the {@link #getMessage()} method.
     */
    public RepositoryException(String message) {
        super(message);
    }
}
