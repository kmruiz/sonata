package io.sonata.lang.parser.ast.type;

import io.sonata.lang.tokenizer.token.IdentifierToken;
import io.sonata.lang.tokenizer.token.OperatorToken;
import io.sonata.lang.tokenizer.token.Token;

public class EmptyType implements Type {
    private static final EmptyType INSTANCE = new EmptyType();

    public static EmptyType instance() {
        return INSTANCE;
    }

    @Override
    public Type consume(Token token) {
        if (token instanceof IdentifierToken) {
            return new BasicType(token.representation());
        }

        if (token instanceof OperatorToken) {
            return PartialFunctionType.withoutParameters();
        }

        return null;
    }

    @Override
    public String representation() {
        return "<empty type>";
    }
}
