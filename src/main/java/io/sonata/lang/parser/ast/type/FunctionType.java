package io.sonata.lang.parser.ast.type;

import java.util.List;
import java.util.stream.Collectors;

public class FunctionType extends ComposedType implements Type {
    public final List<Type> parameters;
    public final Type returnType;

    public FunctionType(List<Type> parameters, Type returnType) {
        this.parameters = parameters;
        this.returnType = returnType;
    }

    @Override
    public String representation() {
        return "let(" + parameters.stream().map(Object::toString).collect(Collectors.joining(",")) + "): " + returnType.toString();
    }
}
