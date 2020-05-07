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
     * Create a new {@link Pair} object.
     * @param t the first element
     * @param u the second element
     */
    public Pair(T t, U u) {
        this.first = t;
        this.second = u;
    }

    /**
     * A convenient way to construct a {@link Pair} object.
     * <p>
     * Sorry, diamond operator.
     *
     * @param <T> Type parameter of first element
     * @param <U> Type parameter of second element
     * @param t the first element
     * @param u the second element
     * @return the {@link Pair} object same as {@code new Pair<>(t, u)}.
     */
    public static <T, U> Pair<T, U> of(T t, U u) {
        return new Pair<>(t, u);
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
        Pair<?, ?> pair = (Pair<?, ?>) o;
        return first.equals(pair.first) &&
                second.equals(pair.second);
    }

    /**
     * {@inheritDoc}
     * @return hashCode of this object.
     */
    @Override
    public int hashCode() {
        return Objects.hash(first, second);
    }
}
