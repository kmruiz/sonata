/*
 * Copyright (c) 2020 Kevin Mas Ruiz
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package io.sonata.lang.e2e;

import io.reactivex.Flowable;
import io.sonata.lang.backend.js.JavaScriptBackend;
import io.sonata.lang.cli.Sonata;
import io.sonata.lang.exception.SonataSyntaxError;
import io.sonata.lang.log.CompilerLog;
import io.sonata.lang.source.Source;
import org.graalvm.polyglot.Context;
import org.mockito.Mockito;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.joining;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class E2ETest {
    private final ByteArrayOutputStream proxyOutput = new ByteArrayOutputStream();
    private final Context jsContext = Context.newBuilder("js")
            .allowAllAccess(true)
            .out(proxyOutput)
            .build();

    protected final void assertResourceScriptOutputs(String expectedOutput, String resource) {
        String script = getLiteralResource(resource);
        assertScriptOutputs(expectedOutput, script);
    }

    protected final void assertSyntaxError(String errorMessage, String resource) {
        List<SonataSyntaxError> syntaxErrors = new ArrayList<>();
        CompilerLog mockLog = Mockito.mock(CompilerLog.class);
        Mockito.doAnswer(e -> {
            syntaxErrors.add(e.getArgument(0, SonataSyntaxError.class));
            return null;
        }).when(mockLog).syntaxError(Mockito.any());

        compileToString(mockLog, getLiteralResource(resource));
        boolean found = syntaxErrors.stream().anyMatch(p -> p.message().contains(errorMessage));
        assertTrue(found, "Could not find a syntax error containing the following error message: " + errorMessage + "\n Found errors: " + syntaxErrors.stream().map(SonataSyntaxError::message).collect(joining("\n")));
    }

    private void assertScriptOutputs(String expectedOutput, String literalScript) {
        String output = executeScript(literalScript);
        assertEquals(expectedOutput.trim(), output.trim().replaceAll("\\n{2,}", "\n"));
    }

    private String executeScript(String literalScript) {
        String compiledVersion = compileToString(CompilerLog.console(), literalScript);
        System.out.println(">> Source Code:\n" + literalScript);
        System.out.println(">> JavaScript:\n" + compiledVersion);

        proxyOutput.reset();
        jsContext.eval("js", compiledVersion);
        return new String(proxyOutput.toByteArray());
    }

    private String compileToString(CompilerLog log, String literalScript) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(baos));

        Source literalSource = Source.fromLiteral(literalScript);
        Sonata.compile(log, Flowable.just(literalSource), new JavaScriptBackend(writer)).blockingAwait();

        return new String(baos.toByteArray());
    }

    private String getLiteralResource(String resource) {
        InputStream stream = this.getClass().getResourceAsStream("/e2e/" + resource + ".sn");
        return new BufferedReader(new InputStreamReader(stream)).lines().collect(joining("\n"));
    }
}
