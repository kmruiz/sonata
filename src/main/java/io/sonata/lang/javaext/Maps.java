package io.sonata.lang.javaext;

import java.util.LinkedHashMap;
import java.util.Map;

public final class Maps {
    public static <K, V> Map<K, V> with(Map<K, V> origin, K key, V value) {
        Map<K, V> newMap = new LinkedHashMap<>(origin);
        newMap.put(key, value);
        return newMap;
    }
}
