package io.sonata.lang.parser.ast.exp;

import io.sonata.lang.source.SourcePosition;
import io.sonata.lang.tokenizer.token.NumericToken;
import io.sonata.lang.tokenizer.token.SeparatorToken;
import io.sonata.lang.tokenizer.token.Token;

public class PartialArrayAccess implements Expression {
    public final SourcePosition definition;
    public final Expression receiver;
    public final String index;

    private PartialArrayAccess(SourcePosition definition, Expression receiver, String index) {
        this.definition = definition;
        this.receiver = receiver;
        this.index = index;
    }

    public static PartialArrayAccess on(SourcePosition definition, Expression receiver) {
        return new PartialArrayAccess(definition, receiver, null);
    }

    @Override
    public Expression consume(Token token) {
        if (token instanceof NumericToken && index == null) {
            NumericToken idx = (NumericToken) token;
            return new PartialArrayAccess(definition, receiver, idx.value);
        }

        if (token instanceof SeparatorToken && index != null) {
            SeparatorToken sep = (SeparatorToken) token;
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

    @Override
    public SourcePosition definition() {
        return definition;
    }
}
