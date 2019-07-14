package io.sonata.lang.parser.ast;

import io.sonata.lang.parser.ast.exp.Atom;
import io.sonata.lang.parser.ast.exp.PartialPriorityExpression;
import io.sonata.lang.parser.ast.let.PartialLet;
import io.sonata.lang.tokenizer.token.IdentifierToken;
import io.sonata.lang.tokenizer.token.SeparatorToken;
import io.sonata.lang.tokenizer.token.Token;

public class RootNode implements Node {
    private static final RootNode INSTANCE = new RootNode();

    public static RootNode instance() {
        return INSTANCE;
    }

    @Override
    public String representation() {
        return "<root>";
    }

    @Override
    public Node consume(Token token) {
        if (token instanceof IdentifierToken) {
            if (((IdentifierToken) token).value.equals("let")) {
                return PartialLet.initial();
            }

            return new Atom(token.representation());
        }

        if (token instanceof SeparatorToken) {
            if (token.representation().equals("\n")) {
                return this;
            }

            if (token.representation().equals("(")) {
                return PartialPriorityExpression.instance();
            }
        }

        return null;
    }
}
