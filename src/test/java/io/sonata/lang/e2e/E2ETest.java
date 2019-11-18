package io.sonata.lang.e2e;

import io.sonata.lang.cli.command.Compile;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.OutputFrame;
import org.testcontainers.containers.output.WaitingConsumer;
import org.testcontainers.utility.MountableFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Collections;
import java.util.stream.Collectors;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static org.junit.jupiter.api.Assertions.assertEquals;

public abstract class E2ETest {
    protected final void assertResourceScriptOutputs(String expectedOutput, String resource) throws Exception {
        InputStream stream = this.getClass().getResourceAsStream("/e2e/" + resource + ".sn");
        String script = new BufferedReader(new InputStreamReader(stream))
                .lines().collect(Collectors.joining("\n"));

        assertScriptOutputs(expectedOutput, script);
    }

    private void assertScriptOutputs(String expectedOutput, String literalScript) throws Exception {
        WaitingConsumer waitingConsumer = new WaitingConsumer();
        GenericContainer container = executeScript(literalScript);

        container.followOutput(waitingConsumer, OutputFrame.OutputType.STDOUT);
        waitingConsumer.waitUntilEnd();

        assertEquals(expectedOutput.trim(), container.getLogs().trim().replaceAll("\\n{2,}", "\n"));
    }

    private GenericContainer executeScript(String literalScript) throws Exception {
        String compiledVersion = compileToTemporalPath(literalScript);
        System.out.println(">> Source Code:\n" + literalScript);
        System.out.println(">> JavaScript:\n" + readString(Paths.get(compiledVersion)));

        GenericContainer container = new GenericContainer("node:12-alpine")
                .withCopyFileToContainer(MountableFile.forHostPath(compiledVersion), "./script.js")
                .withCommand("node script.js")
                .withStartupAttempts(1)
                .withStartupTimeout(Duration.ofSeconds(10));

        container.start();
        return container;
    }

    private String compileToTemporalPath(String literalScript) throws Exception {
        String file = File.createTempFile("io.sonata.lang.e2e", ".input.sn").getAbsolutePath();
        String output = File.createTempFile("io.sonata.lang.e2e", ".output.js").getAbsolutePath();

        Files.write(Paths.get(file), literalScript.getBytes(), CREATE, TRUNCATE_EXISTING);
        Compile.execute(Collections.singletonList(file), output);
        return output;
    }

    private String readString(Path path) throws IOException {
        return new String(Files.readAllBytes(path));
    }
}
