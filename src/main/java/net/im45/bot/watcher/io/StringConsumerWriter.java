package net.im45.bot.watcher.io;

import org.jetbrains.annotations.NotNull;

import java.io.Writer;
import java.util.function.Consumer;

public class StringConsumerWriter extends Writer implements Consumer<String> {
    Consumer<String> out;

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
     * Writes a string.
     *
     * @param str String to be written
     */
    @Override
    public void write(@NotNull String str) {
        accept(str);
    }

    /**
     * Writes a portion of a string.
     *
     * @param str A String
     * @param off Offset from which to start writing characters
     * @param len Number of characters to write
     * @throws IndexOutOfBoundsException Implementations should throw this exception
     *                                   if {@code off} is negative, or {@code len} is negative,
     *                                   or {@code off + len} is negative or greater than the length
     *                                   of the given string
     * @implSpec The implementation in this class throws an
     * {@code IndexOutOfBoundsException} for the indicated conditions;
     * overriding methods may choose to do otherwise.
     */
    @Override
    public void write(@NotNull String str, int off, int len) {
        char[] chars = new char[len];
        str.getChars(off, off + len, chars, 0);
        write(chars, 0, len);
    }

    /**
     * Writes an array of characters.
     *
     * @param cbuf Array of characters to be written
     */
    @Override
    public void write(@NotNull char[] cbuf) {
        write(cbuf, 0, cbuf.length);
    }

    /**
     * Write a portion of an array of characters.
     *
     * @param cbuf Array of characters
     * @param off  Offset from which to start writing characters
     * @param len  Number of characters to write
     * @throws IndexOutOfBoundsException If {@code off} is negative, or {@code len} is negative,
     *                                   or {@code off + len} is negative or greater than the length
     *                                   of the given array
     */
    @Override
    public void write(@NotNull char[] cbuf, int off, int len) {
        // Copied from BufferedWriter
        if ((off < 0) || (off > cbuf.length) || (len < 0) ||
                ((off + len) > cbuf.length) || ((off + len) < 0)) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return;
        }

        char[] chars;
        if (off == 0 && len == cbuf.length) {
            chars = cbuf;
        } else {
            chars = new char[len];
            System.arraycopy(cbuf, off, chars, 0, len);
        }

        write(new String(chars));
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
