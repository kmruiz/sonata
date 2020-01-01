package io.sonata.lang.analyzer.typeSystem;

import io.sonata.lang.source.SourcePosition;

public interface Type {
    SourcePosition definition();
    String name();
    boolean canBeReassigned();
    boolean isEntity();
}
