package io.sonata.lang.exception;

import io.sonata.lang.parser.ast.Node;


public final class SonataSyntaxError {
    private final Node where;
    private final String message;

    public SonataSyntaxError(Node where, String message) {
        this.where = where;
        this.message = message;
    }

    public Node whereHappened() {
        return where;
    }

    public String message() {
        return message;
    }
}
