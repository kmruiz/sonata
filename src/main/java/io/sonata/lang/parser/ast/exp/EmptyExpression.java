package io.sonata.lang.parser.ast.exp;

import io.sonata.lang.tokenizer.token.Token;

public class EmptyExpression implements Expression {
    private static final EmptyExpression INSTANCE = new EmptyExpression();

    public static Expression instance() {
        return INSTANCE;
    }
    @Override
    public Expression consume(Token token) {
        if (token.representation().equals("(")) {
            return PartialPriorityExpression.instance();
        }

        return new Atom(token.representation());
    }

    @Override
    public String representation() {
        return "<empty expression>";
    }
}
