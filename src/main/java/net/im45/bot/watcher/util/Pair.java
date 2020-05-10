package net.im45.bot.watcher.util;

import java.util.Objects;

/**
 * Utility class representing a pair of related objects.
 *
 * @param <T> Type parameter of first element
 * @param <U> Type parameter of second element
 *
 * @author 45gfg9
 */
public class Pair<T, U> {
    /**
     * The first element.
     */
    public final T first;

    /**
     * The second element.
     */
    public final U second;

    /**
     * Private constructor, please use {@link #of(T, U)}
     *
     * @param t the first element
     * @param u the second element
     */
    private Pair(T t, U u) {
        this.first = t;
        this.second = u;
    }

    /**
     * Factory method for creating {@link Pair} objects.
     *
     * @param <T> Type parameter of first element
     * @param <U> Type parameter of second element
     * @param t the first element
     * @param u the second element
     * @return a {@link Pair} object.
     */
    public static <T, U> Pair<T, U> of(T t, U u) {
        return new Pair<>(t, u);
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
        if (o == null || getClass() != o.getClass()) return false;
        Pair<?, ?> pair = (Pair<?, ?>) o;
        return first.equals(pair.first) &&
                second.equals(pair.second);
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(first, second);
    }
}
