package io.sonata.lang.parser.ast.type;

import java.util.List;
import java.util.stream.Collectors;

public class GenericType extends ComposedType implements Type {
    public final Type base;
    public final List<Type> parameters;

    public GenericType(Type base, List<Type> parameters) {
        this.base = base;
        this.parameters = parameters;
    }

    @Override
    public String representation() {
        return base.representation() + "[" + parameters.stream().map(Type::representation).collect(Collectors.joining(", ")) + "]";
    }
}
