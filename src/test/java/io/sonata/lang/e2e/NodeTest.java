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
import io.sonata.lang.log.CompilerLog;
import io.sonata.lang.parser.ast.RequiresPaths;
import io.sonata.lang.source.Source;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.util.stream.Collectors.joining;
import static org.junit.jupiter.api.Assertions.assertEquals;

public abstract class NodeTest {
    private static File TARGET_DIRECTORY;

    static {
        try {
            TARGET_DIRECTORY = Files.createTempDirectory("sonata.e2e").toFile();
        } catch (IOException e) {
            System.err.println(e.getMessage());
            System.exit(-1);
        }
    }

    protected final void assertResourceScriptOutputs(String expectedOutput, String resource) throws Exception {
        Path path = compileToTemporalPath(CompilerLog.console(),getLiteralResource(resource));
        String output = run(path).trim();

        assertEquals(expectedOutput, output);
    }

    protected final String runScriptAndGetOutput(String resource) throws Exception {
        Path path = compileToTemporalPath(CompilerLog.console(), getLiteralResource(resource));
        return run(path);
    }

    private String run(Path path) throws Exception {
        Process process = Runtime.getRuntime().exec("node " + path.toAbsolutePath());
        InputStream inputStream = process.getInputStream();

        return new BufferedReader(new InputStreamReader(inputStream))
                .lines().collect(Collectors.joining("\n"));
    }

    private Path compileToTemporalPath(CompilerLog log, String literalScript) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(baos));

        Source literalSource = Source.fromLiteral(literalScript);
        Sonata.compile(log, Flowable.just(literalSource), RequiresPaths.are(), new JavaScriptBackend(writer)).blockingAwait();
        final File tempFile = File.createTempFile("io.sonata.lang.e2e", ".output.js", TARGET_DIRECTORY);
        String output = tempFile.getAbsolutePath();

        final Path path = Paths.get(output);
        Files.write(path, baos.toByteArray(), CREATE, TRUNCATE_EXISTING);
        return path;
    }

    private String getLiteralResource(String resource) {
        InputStream stream = this.getClass().getResourceAsStream(resource);
        return new BufferedReader(new InputStreamReader(stream)).lines().collect(joining("\n"));
    }
}
