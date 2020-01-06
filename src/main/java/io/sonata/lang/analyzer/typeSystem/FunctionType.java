package io.sonata.lang.analyzer.typeSystem;

import io.sonata.lang.source.SourcePosition;

import java.util.List;

public class FunctionType implements Type {
    private final SourcePosition definition;
    private final String name;
    private final Type returnType;
    private final List<Type> parameters;

    public FunctionType(SourcePosition definition, String name, Type returnType, List<Type> parameters) {
        this.definition = definition;
        this.name = name;
        this.returnType = returnType;
        this.parameters = parameters;
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
        return false;
    }
}
