package io.sonata.lang.parser.ast.exp;

public class IfElse extends ComposedExpression {
    public final Expression condition;
    public final Expression whenTrue;
    public final Expression whenFalse;

    public IfElse(Expression condition, Expression whenTrue, Expression whenFalse) {
        this.condition = condition;
        this.whenTrue = whenTrue;
        this.whenFalse = whenFalse;
    }

    @Override
    public String representation() {
        return "if (" + condition.representation() + ") { " + whenTrue.representation() + " } else { "  + whenFalse.representation() + " }";
    }
}
