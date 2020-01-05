package io.sonata.lang.javaext;

import java.util.HashMap;
import java.util.Map;

public final class Maps {
    public static <K, V> Map<K, V> with(Map<K, V> origin, K key, V value) {
        Map<K, V> newMap = new HashMap<>(origin);
        newMap.put(key, value);
        return newMap;
    }
}
