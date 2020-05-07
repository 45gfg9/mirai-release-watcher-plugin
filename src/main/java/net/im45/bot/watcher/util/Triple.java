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
     * Create a new {@link Triple} object.
     * @param t the first element
     * @param u the second element
     * @param v the third element
     */
    public Triple(T t, U u, V v) {
        this.first = t;
        this.second = u;
        this.third = v;
    }

    /**
     * A convenient way to construct a {@link Triple} object.
     * <p>
     * Sorry, diamond operator.
     *
     * @param <T> Type parameter of first element
     * @param <U> Type parameter of second element
     * @param <V> Type parameter of third element
     * @param t the first element
     * @param u the second element
     * @param v the third element
     * @return the {@link Triple} object same as {@code new Triple<>(t, u, v)}.
     */
    public static <T, U, V> Triple<T, U, V> of(T t, U u, V v) {
        return new Triple<>(t, u, v);
    }

    /**
     * {@inheritDoc}
     * @param o another object
     * @return if two objects are equal
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
     * @return hashCode of this object.
     */
    @Override
    public int hashCode() {
        return Objects.hash(first, second, third);
    }
}
