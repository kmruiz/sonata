package io.sonata.lang.parser.ast.type;

import io.sonata.lang.source.SourcePosition;

public class ArrayASTType extends ComposedASTType implements ASTType {
    public final ASTType base;

    public ArrayASTType(ASTType base) {
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
