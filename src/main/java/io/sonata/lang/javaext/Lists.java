package io.sonata.lang.javaext;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public final class Lists {
    @SafeVarargs
    public static <T> List<T> append(List<T> l, T... v) {
        List<T> result = newIfNeed(l);
        result.addAll(Arrays.asList(v));

        return result;
    }

    public static <T> List<T> append(List<T> l, List<T> v) {
        List<T> result = newIfNeed(l);
        result.addAll(v);

        return result;
    }

    private static <T> List<T> newIfNeed(List<T> list) {
        return new LinkedList<>(list);
    }
}
