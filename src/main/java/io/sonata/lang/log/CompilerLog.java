package io.sonata.lang.log;

import io.sonata.lang.exception.SonataSyntaxError;
import io.sonata.lang.parser.ast.Node;
import io.sonata.lang.source.SourcePosition;

import java.io.PrintWriter;

public final class CompilerLog {
    public static final String ERROR_TAG = "[ERROR]";
    public static final String INFO_TAG  = "[INFO ]";

    private final PrintWriter info;
    private final PrintWriter error;
    private boolean errored;

    private CompilerLog(PrintWriter info, PrintWriter error) {
        this.info = info;
        this.error = error;
        this.errored = false;
    }

    public static CompilerLog console() {
        return new CompilerLog(new PrintWriter(System.out), new PrintWriter(System.err));
    }

    public void syntaxError(SonataSyntaxError syntaxError) {
        this.errored = true;

        final SourcePosition definition = syntaxError.whereHappened().definition();
        final String definitionScript = clearRepresentationOf(syntaxError.whereHappened());
        final String message = syntaxError.message();

        error.printf("%s %s near '%s': %s\n", ERROR_TAG, definition, definitionScript, message);
        error.flush();
    }

    public void inPhase(String phase) {
        info.printf("%s In phase '%s'.\n", INFO_TAG, phase);
        info.flush();
    }

    public void info(String message) {
        info.printf("%s %s\n", INFO_TAG, message);
        info.flush();
    }

    public boolean hasErrors() {
        return errored;
    }

    private String clearRepresentationOf(Node node) {
        final String representation = node.representation();
        final int newLineIndex = representation.indexOf("\n");
        if (newLineIndex == -1) {
            return representation;
        } else {
            return representation.substring(0, newLineIndex) + "...";
        }
    }
}
