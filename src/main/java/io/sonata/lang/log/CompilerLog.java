/*
 * Copyright (c) 2020 Kevin Mas Ruiz
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package io.sonata.lang.log;

import io.sonata.lang.exception.SonataSyntaxError;
import io.sonata.lang.parser.ast.Node;
import io.sonata.lang.source.SourcePosition;

import java.io.PrintWriter;
import java.text.NumberFormat;

public class CompilerLog {
    public static final String FATAL_TAG = "[FATAL]";
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

    public void requestedModule(String moduleName, boolean isStd) {
        info.printf("%s Requested module %s, which is %s.\n", INFO_TAG, moduleName, isStd ? "standard" : "external");
        info.flush();
    }

    public void loadedModule(String moduleName) {
        info.printf("%s Module %s has been marked as loaded.\n", INFO_TAG, moduleName);
        info.flush();
    }

    public void syntaxError(SonataSyntaxError syntaxError) {
        this.errored = true;

        if (syntaxError.where != null) {
            final SourcePosition definition = syntaxError.where.definition();
            final String definitionScript = clearRepresentationOf(syntaxError.where);
            final String message = syntaxError.message;

            error.printf("%s %s near '%s': %s\n", ERROR_TAG, definition, definitionScript, message);
        } else {
            final SourcePosition definition = syntaxError.definition;
            final String message = syntaxError.message;

            error.printf("%s %s: %s\n", ERROR_TAG, definition, message);
        }

        error.flush();
    }

    public void phaseExecuted(String phase, long timeInMs) {
        info.printf("%s %30s: %8s ms\n", INFO_TAG, "'" + phase + "'", NumberFormat.getIntegerInstance().format(timeInMs));
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
        try {
            final String representation = node.representation();
            final int newLineIndex = representation.indexOf("\n");
            if (newLineIndex == -1) {
                return representation;
            } else {
                return representation.substring(0, newLineIndex) + "...";
            }
        } catch (RuntimeException e) {
            return "";
        }
    }

    public void compilerError(Throwable err) {
        error.printf("%s %s: %s\n", ERROR_TAG, err.getClass().getCanonicalName(), err.getMessage());
        err.printStackTrace(error);
        error.flush();
    }
}
