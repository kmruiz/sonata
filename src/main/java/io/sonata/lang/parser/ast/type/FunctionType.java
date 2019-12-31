package io.sonata.lang.parser.ast.type;

import io.sonata.lang.source.SourcePosition;

import java.util.List;
import java.util.stream.Collectors;

public class FunctionType extends ComposedType implements Type {
    public final SourcePosition definition;
    public final List<Type> parameters;
    public final Type returnType;

    public FunctionType(SourcePosition definition, List<Type> parameters, Type returnType) {
        this.definition = definition;
        this.parameters = parameters;
        this.returnType = returnType;
    }

    @Override
    public String representation() {
        return "let(" + parameters.stream().map(Object::toString).collect(Collectors.joining(",")) + "): " + returnType.toString();
    }

    @Override
    public SourcePosition definition() {
        return definition;
    }
}
