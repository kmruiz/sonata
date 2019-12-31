package io.sonata.lang.parser.ast.type;

import io.sonata.lang.source.SourcePosition;
import io.sonata.lang.tokenizer.token.IdentifierToken;
import io.sonata.lang.tokenizer.token.OperatorToken;
import io.sonata.lang.tokenizer.token.SeparatorToken;
import io.sonata.lang.tokenizer.token.Token;

public class EmptyType implements Type {
    private static final EmptyType INSTANCE = new EmptyType();

    public static EmptyType instance() {
        return INSTANCE;
    }

    @Override
    public Type consume(Token token) {
        if (token instanceof IdentifierToken) {
            return BasicType.named(token.sourcePosition(), token.representation());
        }

        if (token instanceof SeparatorToken && token.representation().equals("(")) {
            return PartialFunctionType.inParameterList(token.sourcePosition());
        }

        if (token instanceof OperatorToken && token.representation().equals("->")) {
            return PartialFunctionType.withoutParameters(token.sourcePosition());
        }

        return null;
    }

    @Override
    public String representation() {
        return "<empty type>";
    }

    @Override
    public SourcePosition definition() {
        return null;
    }
}
