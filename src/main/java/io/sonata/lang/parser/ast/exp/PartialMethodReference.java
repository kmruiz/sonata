package io.sonata.lang.parser.ast.exp;

import io.sonata.lang.tokenizer.token.IdentifierToken;
import io.sonata.lang.tokenizer.token.Token;

public class PartialMethodReference implements Expression {
    public final Expression receiver;

    public PartialMethodReference(Expression receiver) {
        this.receiver = receiver;
    }

    @Override
    public Expression consume(Token token) {
        if (token instanceof IdentifierToken) {
            return new MethodReference(receiver, token.representation());
        }

        return null;
    }

    @Override
    public String representation() {
        return receiver.representation() + ".";
    }
}
