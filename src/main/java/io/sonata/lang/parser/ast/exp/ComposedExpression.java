package io.sonata.lang.parser.ast.exp;

import io.sonata.lang.tokenizer.token.OperatorToken;
import io.sonata.lang.tokenizer.token.SeparatorToken;
import io.sonata.lang.tokenizer.token.Token;

public abstract class ComposedExpression implements Expression {
    @Override
    public Expression consume(Token token) {
        if (token instanceof OperatorToken) {
            if (token.representation().equals("#")) {
                return new TailExtraction(this, 0);
            }

            return SimpleExpression.initial(this, token.representation());
        }

        if (token instanceof SeparatorToken) {
            SeparatorToken sep = (SeparatorToken) token;
            if (sep.separator.equals("(")) {
                return PartialFunctionCall.on(this);
            }

            if (sep.separator.equals(".")) {
                return new PartialMethodReference(this);
            }

            if (sep.separator.equals("[")) {
                return PartialArrayAccess.on(this);
            }
        }

        return null;
    }
}
