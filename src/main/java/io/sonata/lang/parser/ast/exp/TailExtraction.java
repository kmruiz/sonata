package io.sonata.lang.parser.ast.exp;

import io.sonata.lang.source.SourcePosition;

public class TailExtraction extends ComposedExpression implements Expression {
    public final Expression expression;
    public final int fromIndex;

    public TailExtraction(Expression expression, int fromIndex) {
        this.expression = expression;
        this.fromIndex = fromIndex;
    }

    @Override
    public String representation() {
        return expression.representation() + "#";
    }

    @Override
    public SourcePosition definition() {
        return expression.definition();
    }
}
