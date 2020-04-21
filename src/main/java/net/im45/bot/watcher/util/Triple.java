package net.im45.bot.watcher.util;

import java.util.Objects;

public class Triple<T, U, V> {
    public final T first;
    public final U second;
    public final V third;

    public Triple(T t, U u, V v) {
        this.first = t;
        this.second = u;
        this.third = v;
    }

    public static <T, U, V> Triple<T, U, V> of(T t, U u, V v) {
        return new Triple<>(t, u, v);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Triple<?, ?, ?> triple = (Triple<?, ?, ?>) o;
        return first.equals(triple.first) &&
                second.equals(triple.second) &&
                third.equals(triple.third);
    }

    @Override
    public int hashCode() {
        return Objects.hash(first, second, third);
    }
}
