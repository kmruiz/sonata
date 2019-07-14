package io.sonata.lang.javaext;

import java.util.ArrayList;
import java.util.List;

public final class Lists {
    public static <T> List<T> append(List<T> l, T v) {
        var result = new ArrayList<>(List.copyOf(l));
        result.add(v);

        return result;
    }
}
