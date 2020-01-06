package io.sonata.lang.parser.ast.exp;

import io.sonata.lang.source.SourcePosition;
import io.sonata.lang.tokenizer.token.Token;

public class Continuation implements Expression {
    public final SourcePosition definition;
    public final Expression body;

    public Continuation(SourcePosition definition, Expression body) {
        this.definition = definition;
        this.body = body;
    }

    @Override
    public Expression consume(Token token) {
        return null;
    }

    @Override
    public SourcePosition definition() {
        return definition;
    }

    @Override
    public String representation() {
        return "<continuation of> " + body.representation();
    }
}
