package net.im45.bot.watcher.io;

import org.jetbrains.annotations.NotNull;

import java.io.Writer;
import java.util.function.Consumer;

/**
 * A character stream that forwards its output to an underlying {@code Consumer<String>}.
 * <p>
 * Closing a {@code StringConsumerWriter} has no effect. The methods in this class
 * can be called after the stream has been closed without generating an
 * {@code IOException}.
 * <p>
 * This class can be used directly as a {@code Consumer<String>}.
 *
 * @see java.util.function.Consumer
 *
 * @author 45gfg9
 */
public class StringConsumerWriter extends Writer implements Consumer<String> {

    /**
     * The underlying {@code Consumer<String>}.
     */
    private final Consumer<String> out;

    /**
     * Constructs a {@code StringConsumerWriter}.
     *
     * @param out the Consumer.
     */
    public StringConsumerWriter(Consumer<String> out) {
        this.out = out;
    }

    /**
     * Write a single character.
     */
    @Override
    public void write(int c) {
        out.accept(String.valueOf((char) c));
    }

    /**
     * {@inheritDoc}
     *
     * @param str String to be written
     */
    @Override
    public void write(@NotNull String str) {
        accept(str);
    }

    /**
     * {@inheritDoc}
     *
     * @param str A String
     * @param off Offset from which to start writing characters
     * @param len Number of characters to write
     * @throws StringIndexOutOfBoundsException
     *         If {@code off} is negative, or {@code len} is negative,
     *         or {@code off + len} is negative or greater than the length
     *         of the given string
     */
    @Override
    public void write(@NotNull String str, int off, int len) {
        write(str.substring(off, off + len));
    }

    /**
     * {@inheritDoc}
     *
     * @param buf Array of characters to be written
     */
    @Override
    public void write(@NotNull char[] buf) {
        write(buf, 0, buf.length);
    }

    /**
     * {@inheritDoc}
     *
     * @param buf Array of characters
     * @param off  Offset from which to start writing characters
     * @param len  Number of characters to write
     * @throws StringIndexOutOfBoundsException
     *         If {@code off} is negative, or {@code len} is negative,
     *         or {@code off + len} is negative or greater than the length
     *         of the given array
     */
    @Override
    public void write(@NotNull char[] buf, int off, int len) {
        write(new String(buf, off, len));
    }

    /**
     * Flush the stream.
     */
    @Override
    public void flush() {
    }

    /**
     * Closing a {@code StringConsumerWriter} has no effect. The methods in this
     * class can be called after the stream has been closed without generating
     * an {@code IOException}.
     */
    @Override
    public void close() {
    }

    /**
     * Delegates to {@link #out}.
     *
     * @param s the input string
     */
    @Override
    public void accept(String s) {
        out.accept(s);
    }
}
