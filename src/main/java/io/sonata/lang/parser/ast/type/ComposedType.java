package io.sonata.lang.parser.ast.type;

import io.sonata.lang.tokenizer.token.SeparatorToken;
import io.sonata.lang.tokenizer.token.Token;

public abstract class ComposedType implements Type {
    public Type consume(Token token) {
        if (token instanceof SeparatorToken) {
            var sep = (SeparatorToken) token;

            if (sep.separator.equals("[")) {
                return PartialGenericType.on(this);
            }
        }

        return null;
    }
}
