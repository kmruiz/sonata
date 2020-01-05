package io.sonata.lang.parser.ast;

import io.sonata.lang.exception.SonataSyntaxError;
import io.sonata.lang.source.SourcePosition;
import io.sonata.lang.tokenizer.token.Token;

public final class PanicNode implements Node {
    private final SourcePosition definition;

    public PanicNode(SourcePosition definition) {
        this.definition = definition;
    }

    @Override
    public SourcePosition definition() {
        return definition;
    }

    @Override
    public String representation() {
        return "<panic>";
    }

    @Override
    public Node consume(Token token) {
        if (token.representation().equals("\n")) {
            return null;
        }

        return this;
    }
}
