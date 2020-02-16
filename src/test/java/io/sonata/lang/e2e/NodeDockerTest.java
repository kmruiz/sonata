/*
 * Copyright (c) 2020 Kevin Mas Ruiz
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package io.sonata.lang.e2e;

import io.reactivex.rxjava3.core.Flowable;
import io.sonata.lang.backend.js.JavaScriptBackend;
import io.sonata.lang.cli.Sonata;
import io.sonata.lang.exception.SonataSyntaxError;
import io.sonata.lang.log.CompilerLog;
import io.sonata.lang.parser.ast.RequiresPaths;
import io.sonata.lang.source.Source;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Timeout;
import org.mockito.Mockito;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.OutputFrame;
import org.testcontainers.containers.output.WaitingConsumer;
import org.testcontainers.utility.MountableFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.util.stream.Collectors.joining;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Timeout(10)
public abstract class NodeDockerTest {
    protected final void assertResourceScriptOutputs(String expectedOutput, String resource) throws Exception {
        assertScriptOutputs(expectedOutput, getLiteralResource(resource));
    }

    protected final String runScriptAndGetOutput(String resource) throws Exception {
        InputStream stream = this.getClass().getResourceAsStream(resource + ".sn");
        String script = new BufferedReader(new InputStreamReader(stream)).lines().collect(Collectors.joining("\n"));

        return runAndGetOutput(script);
    }

    private void assertScriptOutputs(String expectedOutput, String literalScript) throws Exception {
        assertEquals(expectedOutput.trim(), runAndGetOutput(literalScript));
    }

    protected final void assertSyntaxError(String errorMessage, String resource) throws Exception {
        List<SonataSyntaxError> syntaxErrors = new ArrayList<>();
        CompilerLog mockLog = Mockito.mock(CompilerLog.class);
        Mockito.doAnswer(e -> {
            syntaxErrors.add(e.getArgument(0, SonataSyntaxError.class));
            return null;
        }).when(mockLog).syntaxError(Mockito.any());

        String output = compileToString(mockLog, getLiteralResource(resource));
        System.out.println(">> JavaScript:\n" + output);
        boolean found = syntaxErrors.stream().anyMatch(p -> p.message.contains(errorMessage));
        assertTrue(found, "Could not find a syntax error containing the following error message: " + errorMessage + "\n Found errors: " + syntaxErrors.stream().map(e -> e.message).collect(joining("\n")));
    }

    protected final void assertCompiles(String resource) throws Exception {
        List<SonataSyntaxError> syntaxErrors = new ArrayList<>();
        CompilerLog mockLog = Mockito.mock(CompilerLog.class);
        Mockito.doAnswer(e -> {
            syntaxErrors.add(e.getArgument(0, SonataSyntaxError.class));
            return null;
        }).when(mockLog).syntaxError(Mockito.any());

        String output = compileToString(mockLog, getLiteralResource(resource));
        System.out.println(">> JavaScript:\n" + output);
        boolean empty = syntaxErrors.isEmpty();
        assertTrue(empty, "Could not compile script.\n Found errors: " + syntaxErrors.stream().map(e -> e.message).collect(joining("\n")));
    }

    private GenericContainer executeScript(String literalScript) throws Exception {
        String compiledVersion = compileToTemporalPath(CompilerLog.console(), literalScript);
        System.out.println(">> Source Code:\n" + literalScript);
        System.out.println(">> JavaScript:\n" + readString(Paths.get(compiledVersion)));

        GenericContainer container = new GenericContainer("node:12-alpine")
                .withCopyFileToContainer(MountableFile.forHostPath(compiledVersion), "./script.js")
                .withCommand("node script.js")
                .withStartupAttempts(2)
                .withStartupTimeout(Duration.ofSeconds(5));

        container.start();
        return container;
    }

    private String compileToString(CompilerLog log, String literalScript) throws Exception {
        Path path = Paths.get(compileToTemporalPath(log, literalScript));
        return readString(path);
    }

    private String compileToTemporalPath(CompilerLog log, String literalScript) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(baos));

        Source literalSource = Source.fromLiteral(literalScript);
        Sonata.compile(log, Flowable.just(literalSource), RequiresPaths.are(), new JavaScriptBackend(writer)).blockingAwait();
        String output = File.createTempFile("io.sonata.lang.e2e", ".output.js").getAbsolutePath();

        Files.write(Paths.get(output), baos.toByteArray(), CREATE, TRUNCATE_EXISTING);
        return output;
    }

    private String readString(Path path) throws IOException {
        return new String(Files.readAllBytes(path));
    }

    @NotNull
    private String runAndGetOutput(String script) throws Exception {
        WaitingConsumer waitingConsumer = new WaitingConsumer();
        GenericContainer container = executeScript(script);

        container.followOutput(waitingConsumer, OutputFrame.OutputType.STDOUT);
        waitingConsumer.waitUntilEnd();

        return container.getLogs().trim().replaceAll("\\n{2,}", "\n");
    }

    private String getLiteralResource(String resource) {
        InputStream stream = this.getClass().getResourceAsStream("/e2e/" + resource + ".sn");
        return new BufferedReader(new InputStreamReader(stream)).lines().collect(joining("\n"));
    }
}
