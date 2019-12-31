package io.sonata.lang.parser.ast.type;

import io.sonata.lang.source.SourcePosition;

public class ArrayType extends ComposedType implements Type {
    public final Type base;

    public ArrayType(Type base) {
        this.base = base;
    }

    @Override
    public String representation() {
        return base.representation() + "[]";
    }

    @Override
    public SourcePosition definition() {
        return base.definition();
    }
}
