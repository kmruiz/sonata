package io.sonata.lang.parser.ast.type;

public class ArrayType extends ComposedType implements Type {
    public final Type base;

    public ArrayType(Type base) {
        this.base = base;
    }

    @Override
    public String representation() {
        return base.representation() + "[]";
    }
}
