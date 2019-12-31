package io.sonata.lang.analyzer.typeSystem;

import io.sonata.lang.source.SourcePosition;

public final class MutableType implements Type {
    private final SourcePosition definition;

    public MutableType(SourcePosition definition) {
        this.definition = definition;
    }

    @Override
    public SourcePosition definition() {
        return definition;
    }

    @Override
    public String name() {
        return "final";
    }

    @Override
    public boolean canBeReassigned() {
        return true;
    }
}
