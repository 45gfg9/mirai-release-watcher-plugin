package net.im45.bot.watcher.util;

import java.util.Objects;

/**
 * Utility class representing three related objects.
 *
 * @param <T> Type parameter of first element
 * @param <U> Type parameter of second element
 * @param <V> Type parameter of third element
 *
 * @author 45gfg9
 */
public class Triple<T, U, V> {
    /**
     * The first element.
     */
    public final T first;

    /**
     * The second element.
     */
    public final U second;

    /**
     * The third element.
     */
    public final V third;

    /**
     * Private constructor, please use {@link #of(T, U, V)}
     *
     * @param t the first element
     * @param u the second element
     * @param v the third element
     */
    private Triple(T t, U u, V v) {
        this.first = t;
        this.second = u;
        this.third = v;
    }

    /**
     * Factory method for creating {@link Triple} objects.
     *
     * @param <T> Type parameter of first element
     * @param <U> Type parameter of second element
     * @param <V> Type parameter of third element
     * @param t the first element
     * @param u the second element
     * @param v the third element
     * @return a {@link Triple} object.
     */
    public static <T, U, V> Triple<T, U, V> of(T t, U u, V v) {
        return new Triple<>(t, u, v);
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
        Triple<?, ?, ?> triple = (Triple<?, ?, ?>) o;
        return first.equals(triple.first) &&
                second.equals(triple.second) &&
                third.equals(triple.third);
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(first, second, third);
    }
}
