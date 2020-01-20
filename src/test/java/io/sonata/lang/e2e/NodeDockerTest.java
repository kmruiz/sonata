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
import io.sonata.lang.log.CompilerLog;
import io.sonata.lang.parser.ast.RequiresPaths;
import io.sonata.lang.source.Source;
import org.jetbrains.annotations.NotNull;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.OutputFrame;
import org.testcontainers.containers.output.WaitingConsumer;
import org.testcontainers.utility.MountableFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.stream.Collectors;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static org.junit.jupiter.api.Assertions.assertEquals;

public abstract class NodeDockerTest {
    protected final void assertResourceScriptOutputs(String expectedOutput, String resource) throws Exception {
        InputStream stream = this.getClass().getResourceAsStream("/e2e/" + resource + ".sn");
        String script = new BufferedReader(new InputStreamReader(stream))
                .lines().collect(Collectors.joining("\n"));

        assertScriptOutputs(expectedOutput, script);
    }

    protected final String runScriptAndGetOutput(String resource) throws Exception {
        InputStream stream = this.getClass().getResourceAsStream(resource + ".sn");
        String script = new BufferedReader(new InputStreamReader(stream)).lines().collect(Collectors.joining("\n"));

        return runAndGetOutput(script);
    }

    private void assertScriptOutputs(String expectedOutput, String literalScript) throws Exception {
        assertEquals(expectedOutput.trim(), runAndGetOutput(literalScript));
    }

    private GenericContainer executeScript(String literalScript) throws Exception {
        String compiledVersion = compileToTemporalPath(literalScript);
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

    private String compileToTemporalPath(String literalScript) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(baos));

        Source literalSource = Source.fromLiteral(literalScript);
        Sonata.compile(CompilerLog.console(), Flowable.just(literalSource), RequiresPaths.are(), new JavaScriptBackend(writer)).blockingAwait();
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
}
