package io.sonata.lang.e2e;

import io.sonata.lang.cli.command.Compile;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.OutputFrame;
import org.testcontainers.containers.output.WaitingConsumer;
import org.testcontainers.utility.MountableFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

public abstract class E2ETest {
    protected final void assertResourceScriptOutputs(String expectedOutput, String resource) throws Exception {
        var stream = this.getClass().getResourceAsStream("/e2e/" + resource + ".sn");
        var script = new BufferedReader(new InputStreamReader(stream))
                .lines().collect(Collectors.joining("\n"));

        assertScriptOutputs(expectedOutput, script);
    }

    private void assertScriptOutputs(String expectedOutput, String literalScript) throws Exception {
        var waitingConsumer = new WaitingConsumer();
        var container = executeScript(literalScript);

        container.followOutput(waitingConsumer, OutputFrame.OutputType.STDOUT);
        waitingConsumer.waitUntilEnd();

        assertEquals(expectedOutput.trim(), container.getLogs().trim().replaceAll("\\n{2,}", "\n"));
    }

    private GenericContainer executeScript(String literalScript) throws Exception {
        String compiledVersion = compileToTemporalPath(literalScript);
        System.out.println(">> Source Code:\n" + literalScript);
        System.out.println(">> JavaScript:\n" + Files.readString(Path.of(compiledVersion)));

        var container = new GenericContainer("node:12-alpine")
                .withCopyFileToContainer(MountableFile.forHostPath(compiledVersion), "./script.js")
                .withCommand("node script.js");

        container.start();
        return container;
    }

    private String compileToTemporalPath(String literalScript) throws Exception {
        var file = File.createTempFile("io.sonata.lang.e2e", ".input.sn").getAbsolutePath();
        var output = File.createTempFile("io.sonata.lang.e2e", ".output.js").getAbsolutePath();

        Files.writeString(Path.of(file), literalScript, Charset.defaultCharset());
        Compile.execute(Collections.singletonList(file), output);
        return output;
    }
}
