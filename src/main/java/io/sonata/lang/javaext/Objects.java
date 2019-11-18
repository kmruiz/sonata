package io.sonata.lang.javaext;

public final class Objects {
    public static <T> T requireNonNullElse(T nullable, T elseValue) {
        return nullable == null ? elseValue : nullable;
    }
}
