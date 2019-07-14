package io.sonata.lang.parser.ast.exp;

public class TailExtraction extends ComposedExpression implements Expression {
    public final Expression expression;

    public TailExtraction(Expression expression) {
        this.expression = expression;
    }

    @Override
    public String representation() {
        return expression.representation() + "#";
    }
}
