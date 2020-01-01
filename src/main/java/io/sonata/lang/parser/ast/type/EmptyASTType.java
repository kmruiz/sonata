package io.sonata.lang.parser.ast.type;

import io.sonata.lang.source.SourcePosition;
import io.sonata.lang.tokenizer.token.IdentifierToken;
import io.sonata.lang.tokenizer.token.OperatorToken;
import io.sonata.lang.tokenizer.token.SeparatorToken;
import io.sonata.lang.tokenizer.token.Token;

public class EmptyASTType implements ASTType {
    private static final EmptyASTType INSTANCE = new EmptyASTType();

    public static EmptyASTType instance() {
        return INSTANCE;
    }

    @Override
    public ASTType consume(Token token) {
        if (token instanceof IdentifierToken) {
            return BasicASTType.named(token.sourcePosition(), token.representation());
        }

        if (token instanceof SeparatorToken && token.representation().equals("(")) {
            return PartialFunctionASTType.inParameterList(token.sourcePosition());
        }

        if (token instanceof OperatorToken && token.representation().equals("->")) {
            return PartialFunctionASTType.withoutParameters(token.sourcePosition());
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
