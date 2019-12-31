package io.sonata.lang.analyzer.typeSystem;

import io.sonata.lang.source.SourcePosition;

import java.util.List;
import java.util.stream.Collectors;

public final class UnionType implements Type {
    private final SourcePosition definition;
    private final List<Type> types;

    public UnionType(SourcePosition definition, List<Type> types) {
        this.definition = definition;
        this.types = types;
    }

    @Override
    public SourcePosition definition() {
        return definition;
    }

    @Override
    public String name() {
        return types.stream().map(Type::name).collect(Collectors.joining(" | "));
    }

    @Override
    public boolean canBeReassigned() {
        return types.stream().anyMatch(Type::canBeReassigned);
    }
}
