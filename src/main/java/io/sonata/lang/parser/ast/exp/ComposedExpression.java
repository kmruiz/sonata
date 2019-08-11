package io.sonata.lang.parser.ast.exp;

import io.sonata.lang.tokenizer.token.OperatorToken;
import io.sonata.lang.tokenizer.token.SeparatorToken;
import io.sonata.lang.tokenizer.token.Token;

import java.util.Objects;

public abstract class ComposedExpression implements Expression {
    @Override
    public Expression consume(Token token) {
        if (token instanceof OperatorToken) {
            if (token.representation().equals("#")) {
                return new TailExtraction(this);
            }

            return SimpleExpression.initial(this, token.representation());
        }

        if (token instanceof SeparatorToken) {
            var sep = (SeparatorToken) token;
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

    @Override
    public int hashCode() {
        return representation().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return (obj != null) && Objects.equals(obj.hashCode(), hashCode());
    }
}
