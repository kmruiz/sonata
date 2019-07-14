package io.sonata.lang.parser.ast.let;

import io.sonata.lang.parser.ast.Node;
import io.sonata.lang.tokenizer.token.IdentifierToken;
import io.sonata.lang.tokenizer.token.SeparatorToken;
import io.sonata.lang.tokenizer.token.Token;

public class PartialLet implements Node {
    public final String letName;

    private PartialLet(String letName) {
        this.letName = letName;
    }

    public static PartialLet initial() {
        return new PartialLet(null);
    }

    @Override
    public String representation() {
        return "let " + (letName == null ? "?" : letName);
    }

    @Override
    public Node consume(Token token) {
        if (letName == null) {
            if (token instanceof IdentifierToken) {
                return new PartialLet(((IdentifierToken) token).value);
            }
        } else {
            if (token instanceof SeparatorToken) {
                var sep = (SeparatorToken) token;

                if (sep.separator.equals("(")) {
                    return PartialLetFunction.initial(letName);
                }
            }
        }

        return null;
    }
}
