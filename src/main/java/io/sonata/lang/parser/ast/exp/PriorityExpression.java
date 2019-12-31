package io.sonata.lang.parser.ast.exp;

import io.sonata.lang.source.SourcePosition;

public class PriorityExpression extends ComposedExpression implements Expression {
    public final Expression expression;

    public PriorityExpression(Expression expression) {
        this.expression = expression;
    }

    @Override
    public String representation() {
        return "(" + expression.representation() + ")";
    }

    @Override
    public SourcePosition definition() {
        return expression.definition();
    }
}
