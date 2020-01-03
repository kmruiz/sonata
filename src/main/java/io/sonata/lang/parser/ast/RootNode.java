package io.sonata.lang.parser.ast;

import io.sonata.lang.parser.ast.classes.entities.PartialEntityClass;
import io.sonata.lang.parser.ast.classes.values.PartialValueClass;
import io.sonata.lang.parser.ast.exp.Atom;
import io.sonata.lang.parser.ast.exp.EmptyExpression;
import io.sonata.lang.parser.ast.exp.ifelse.PartialIf;
import io.sonata.lang.parser.ast.let.PartialLet;
import io.sonata.lang.parser.ast.requires.PartialRequiresNode;
import io.sonata.lang.source.SourcePosition;
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
            switch (token.representation()) {
                case "let":
                    return PartialLet.initial(token.sourcePosition());
                case "requires":
                    return PartialRequiresNode.initial(token.sourcePosition());
                case "entity":
                    return PartialEntityClass.initial(token.sourcePosition());
                case "value":
                    return PartialValueClass.initial(token.sourcePosition());
                case "if":
                    return PartialIf.initial(token.sourcePosition());
            }

            return new Atom(token.sourcePosition(), token.representation());
        }

        if (token instanceof SeparatorToken && token.representation().equals("\n")) {
            return this;
        }

        return EmptyExpression.instance().consume(token);
    }

    @Override
    public SourcePosition definition() {
        return null;
    }
}
