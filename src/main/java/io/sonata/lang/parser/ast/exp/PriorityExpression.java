package io.sonata.lang.parser.ast.exp;

public class PriorityExpression extends ComposedExpression implements Expression {
    public final Expression expression;

    public PriorityExpression(Expression expression) {
        this.expression = expression;
    }

    @Override
    public String representation() {
        return "(" + expression.representation() + ")";
    }
}
