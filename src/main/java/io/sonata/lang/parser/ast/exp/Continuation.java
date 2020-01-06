package io.sonata.lang.parser.ast.exp;

import io.sonata.lang.source.SourcePosition;
import io.sonata.lang.tokenizer.token.Token;

public class Continuation implements Expression {
    public final SourcePosition definition;
    public final Expression body;
    public final boolean fanOut;

    public Continuation(SourcePosition definition, Expression body, boolean fanOut) {
        this.definition = definition;
        this.body = body;
        this.fanOut = fanOut;
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
