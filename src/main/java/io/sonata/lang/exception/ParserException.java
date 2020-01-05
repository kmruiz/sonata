package io.sonata.lang.exception;

import io.sonata.lang.parser.ast.Node;

public final class ParserException extends RuntimeException {
    private final SonataSyntaxError syntaxError;

    public ParserException(SonataSyntaxError syntaxError) {
        this.syntaxError = syntaxError;
    }

    public ParserException(Node where, String cause) {
        this(new SonataSyntaxError(where, cause));
    }

    public SonataSyntaxError syntaxError() {
        return syntaxError;
    }
}
