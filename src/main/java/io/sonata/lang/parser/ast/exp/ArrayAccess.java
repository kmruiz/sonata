package io.sonata.lang.parser.ast.exp;

import io.sonata.lang.source.SourcePosition;

public class ArrayAccess extends ComposedExpression implements Expression {
    public final Expression receiver;
    public final String index;

    public ArrayAccess(Expression receiver, String index) {
        this.receiver = receiver;
        this.index = index;
    }

    @Override
    public String representation() {
        return receiver.representation() + "[" + index + "]";
    }

    @Override
    public SourcePosition definition() {
        return receiver.definition();
    }
}
