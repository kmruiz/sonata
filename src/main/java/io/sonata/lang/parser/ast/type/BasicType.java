package io.sonata.lang.parser.ast.type;

public class BasicType extends ComposedType implements Type {
    public final String name;

    public BasicType(String name) {
        this.name = name;
    }

    @Override
    public String representation() {
        return name;
    }
}
