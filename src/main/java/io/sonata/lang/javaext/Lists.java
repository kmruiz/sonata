package io.sonata.lang.javaext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class Lists {
    @SafeVarargs
    public static <T> List<T> append(List<T> l, T... v) {
        var result = new ArrayList<>(List.copyOf(l));
        result.addAll(Arrays.asList(v));

        return result;
    }

    public static <T> List<T> append(List<T> l, List<T> v) {
        var result = new ArrayList<>(List.copyOf(l));
        result.addAll(v);

        return result;
    }
}
