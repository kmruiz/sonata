package io.sonata.lang.analyzer.log;

import io.sonata.lang.exception.SonataSyntaxErrorException;

import java.io.PrintWriter;

public final class CompilerLog {
    public static final String ERROR_TAG = "[ERROR]";
    public static final String INFO_TAG  = "[INFO ]";

    private final PrintWriter info;
    private final PrintWriter error;

    private CompilerLog(PrintWriter info, PrintWriter error) {
        this.info = info;
        this.error = error;
    }

    public static CompilerLog console() {
        return new CompilerLog(new PrintWriter(System.out), new PrintWriter(System.err));
    }

    public void syntaxError(SonataSyntaxErrorException syntaxError) {
        error.printf("%s %s > near %s: %s\n", ERROR_TAG, syntaxError.whereHappened().definition(), syntaxError.whereHappened().representation(), syntaxError.getMessage());
        error.flush();
    }

    public void inPhase(String phase) {
        info.printf("%s > In phase '%s'\n", INFO_TAG, phase);
        info.flush();
    }
}
