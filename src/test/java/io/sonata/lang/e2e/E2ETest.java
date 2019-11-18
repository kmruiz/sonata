package io.sonata.lang.e2e;

import io.sonata.lang.cli.command.Compile;
import org.graalvm.polyglot.Context;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.util.stream.Collectors.joining;
import static org.junit.jupiter.api.Assertions.assertEquals;

public abstract class E2ETest {
    protected final void assertResourceScriptOutputs(String expectedOutput, String resource) throws Exception {
        InputStream stream = this.getClass().getResourceAsStream("/e2e/" + resource + ".sn");
        String script = new BufferedReader(new InputStreamReader(stream)).lines().collect(joining("\n"));

        assertScriptOutputs(expectedOutput, script);
    }

    private void assertScriptOutputs(String expectedOutput, String literalScript) throws Exception {
        String output = executeScript(literalScript);
        assertEquals(expectedOutput.trim(), output.trim().replaceAll("\\n{2,}", "\n"));
    }

    private String executeScript(String literalScript) throws Exception {
        String compiledVersion = compileToString(literalScript);
        System.out.println(">> Source Code:\n" + literalScript);
        System.out.println(">> JavaScript:\n" + compiledVersion);

        ByteArrayOutputStream proxyOutput = new ByteArrayOutputStream();
        try (
                Context jsContext = Context.newBuilder("js")
                        .out(proxyOutput)
                        .build()
        ) {
            jsContext.eval("js", compiledVersion);
            return new String(proxyOutput.toByteArray());
        }
    }

    private String compileToString(String literalScript) throws Exception {
        String file = File.createTempFile("io.sonata.lang.e2e", ".input.sn").getAbsolutePath();
        String output = File.createTempFile("io.sonata.lang.e2e", ".output.js").getAbsolutePath();

        Files.write(Paths.get(file), literalScript.getBytes(), CREATE, TRUNCATE_EXISTING);
        Compile.execute(Collections.singletonList(file), output);
        return readString(Paths.get(output));
    }

    private String readString(Path path) throws IOException {
        return new String(Files.readAllBytes(path));
    }
}
