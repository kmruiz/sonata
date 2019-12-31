package io.sonata.lang.parser.ast.exp;

import io.sonata.lang.source.SourcePosition;

public class IfElse extends ComposedExpression {
    public final SourcePosition definition;
    public final Expression condition;
    public final Expression whenTrue;
    public final Expression whenFalse;

    public IfElse(SourcePosition definition, Expression condition, Expression whenTrue, Expression whenFalse) {
        this.definition = definition;
        this.condition = condition;
        this.whenTrue = whenTrue;
        this.whenFalse = whenFalse;
    }

    public int weight() {
        return weightOf(condition);
    }

    private int weightOf(Expression expr) {
        if (expr instanceof SimpleExpression) {
            return weightOf(((SimpleExpression) expr).leftSide) + weightOf(((SimpleExpression) expr).rightSide);
        }

        return 1;
    }

    @Override
    public String representation() {
        return "if (" + condition.representation() + ") { " + whenTrue.representation() + " } else { "  + whenFalse.representation() + " }";
    }

    public static int weightedComparison(Expression a, Expression b) {
        if (a instanceof IfElse && b instanceof IfElse) {
            return ((IfElse) b).weight() - ((IfElse) a).weight();
        }

        if (a instanceof IfElse) {
            return -1;
        }

        if (b instanceof IfElse) {
            return 1;
        }

        return 0;
    }

    @Override
    public SourcePosition definition() {
        return definition;
    }
}
