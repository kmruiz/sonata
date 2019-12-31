package io.sonata.lang.exception;

import io.sonata.lang.parser.ast.Node;

public class SonataSyntaxErrorException extends RuntimeException {
    public final Node where;

    public SonataSyntaxErrorException(Node where, String message) {
        super(message);
        this.where = where;
    }

    public Node whereHappened() {
        return where;
    }
}
