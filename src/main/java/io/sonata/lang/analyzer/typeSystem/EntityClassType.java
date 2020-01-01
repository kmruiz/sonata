package io.sonata.lang.analyzer.typeSystem;

import io.sonata.lang.source.SourcePosition;

public final class EntityClassType implements Type {
    private final SourcePosition definition;
    private final String name;

    public EntityClassType(SourcePosition definition, String name) {
        this.definition = definition;
        this.name = name;
    }

    @Override
    public SourcePosition definition() {
        return definition;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public boolean canBeReassigned() {
        return false;
    }

    @Override
    public boolean isEntity() {
        return true;
    }
}
