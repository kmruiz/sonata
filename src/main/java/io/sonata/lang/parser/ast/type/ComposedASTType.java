package io.sonata.lang.parser.ast.type;

import io.sonata.lang.tokenizer.token.SeparatorToken;
import io.sonata.lang.tokenizer.token.Token;

public abstract class ComposedASTType implements ASTType {
    public ASTType consume(Token token) {
        if (token instanceof SeparatorToken) {
            SeparatorToken sep = (SeparatorToken) token;

            if (sep.separator.equals("[")) {
                return PartialGenericASTType.on(this);
            }
        }

        return null;
    }
}
