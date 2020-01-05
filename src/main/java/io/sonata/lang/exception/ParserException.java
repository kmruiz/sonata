package io.sonata.lang.exception;

public final class ParserException extends RuntimeException {
    private final SonataSyntaxError syntaxError;

    public ParserException(SonataSyntaxError syntaxError) {
        this.syntaxError = syntaxError;
    }

    public SonataSyntaxError syntaxError() {
        return syntaxError;
    }
}
