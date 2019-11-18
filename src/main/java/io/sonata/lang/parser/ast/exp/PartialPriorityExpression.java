package io.sonata.lang.parser.ast.exp;

import io.sonata.lang.tokenizer.token.SeparatorToken;
import io.sonata.lang.tokenizer.token.Token;

public class PartialPriorityExpression implements Expression {
    public final Expression expression;

    public PartialPriorityExpression(Expression expression) {
        this.expression = expression;
    }

    public static PartialPriorityExpression instance() {
        return new PartialPriorityExpression(EmptyExpression.instance());
    }

    @Override
    public String representation() {
        return "(" + expression.representation() + ")";
    }

    @Override
    public Expression consume(Token token) {
        Expression next = expression.consume(token);
        if (next == null) {
            if (token instanceof SeparatorToken) {
                SeparatorToken sep = (SeparatorToken) token;
                if (sep.separator.equals(")")) {
                    return new PriorityExpression(expression);
                }
            }
        }

        return new PartialPriorityExpression(next);
    }
}
