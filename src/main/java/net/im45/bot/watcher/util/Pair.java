package net.im45.bot.watcher.util;

import java.util.Objects;

public class Pair<T, U> {
    public final T first;
    public final U second;

    protected Pair(T t, U u) {
        this.first = t;
        this.second = u;
    }

    public static <T, U> Pair<T, U> of(T t, U u) {
        return new Pair<>(t, u);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pair<?, ?> pair = (Pair<?, ?>) o;
        return first.equals(pair.first) &&
                second.equals(pair.second);
    }

    @Override
    public int hashCode() {
        return Objects.hash(first, second);
    }
}
