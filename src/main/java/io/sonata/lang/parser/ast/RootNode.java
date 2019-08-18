package io.sonata.lang.parser.ast;

import io.sonata.lang.parser.ast.classes.values.PartialValueClass;
import io.sonata.lang.parser.ast.exp.Atom;
import io.sonata.lang.parser.ast.exp.PartialArray;
import io.sonata.lang.parser.ast.exp.PartialPriorityExpression;
import io.sonata.lang.parser.ast.let.PartialLet;
import io.sonata.lang.parser.ast.requires.PartialRequiresNode;
import io.sonata.lang.tokenizer.token.IdentifierToken;
import io.sonata.lang.tokenizer.token.OperatorToken;
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
            switch (token.representation()) {
                case "let":
                    return PartialLet.initial();
                case "requires":
                    return PartialRequiresNode.initial();
                case "entity":
                case "value":
                    return PartialValueClass.initial();
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

            if (token.representation().equals("[")) {
                return PartialArray.initial();
            }

            if (token.representation().equals("?")) {
                return Atom.unknown();
            }
        }

        return null;
    }
}
