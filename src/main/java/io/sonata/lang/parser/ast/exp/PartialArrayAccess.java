package io.sonata.lang.parser.ast.exp;

import io.sonata.lang.tokenizer.token.NumericToken;
import io.sonata.lang.tokenizer.token.SeparatorToken;
import io.sonata.lang.tokenizer.token.Token;

public class PartialArrayAccess implements Expression {
    public final Expression receiver;
    public final String index;

    private PartialArrayAccess(Expression receiver, String index) {
        this.receiver = receiver;
        this.index = index;
    }

    public static PartialArrayAccess on(Expression receiver) {
        return new PartialArrayAccess(receiver, null);
    }

    @Override
    public Expression consume(Token token) {
        if (token instanceof NumericToken && index == null) {
            var idx = (NumericToken) token;
            return new PartialArrayAccess(receiver, idx.value);
        }

        if (token instanceof SeparatorToken && index != null) {
            var sep = (SeparatorToken) token;
            if (sep.separator.equals("]")) {
                return new ArrayAccess(receiver, index);
            }
        }

        return null;
    }

    @Override
    public String representation() {
        return null;
    }
}
