package io.sonata.lang.parser.ast;

import io.sonata.lang.parser.ast.exp.Expression;
import io.sonata.lang.source.SourcePosition;
import io.sonata.lang.tokenizer.token.Token;

public class CommentNode implements Expression {
    private final SourcePosition definition;
    private final String comment;

    public CommentNode(SourcePosition definition, String comment) {
        this.definition = definition;
        this.comment = comment;
    }

    @Override
    public SourcePosition definition() {
        return definition;
    }

    @Override
    public String representation() {
        return ";" + comment;
    }

    @Override
    public Expression consume(Token token) {
        return null;
    }
}
