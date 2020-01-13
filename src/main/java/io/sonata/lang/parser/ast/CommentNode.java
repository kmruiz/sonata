package io.sonata.lang.parser.ast;

import io.sonata.lang.parser.ast.exp.Expression;
import io.sonata.lang.source.SourcePosition;
import io.sonata.lang.tokenizer.token.Token;

public class CommentNode implements Expression {
    private final SourcePosition definition;

    public CommentNode(SourcePosition definition) {
        this.definition = definition;
    }

    @Override
    public SourcePosition definition() {
        return definition;
    }

    @Override
    public String representation() {
        return "# <comment>";
    }

    @Override
    public Expression consume(Token token) {
        if (token.representation().equals("\n")) {
            return null;
        }

        return this;
    }
}
