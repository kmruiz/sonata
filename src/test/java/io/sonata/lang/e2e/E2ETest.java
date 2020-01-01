package io.sonata.lang.e2e;

import io.reactivex.Flowable;
import io.sonata.lang.backend.js.JSBackend;
import io.sonata.lang.cli.Sonata;
import io.sonata.lang.log.CompilerLog;
import io.sonata.lang.source.Source;
import org.graalvm.polyglot.Context;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

import static java.util.stream.Collectors.joining;
import static org.junit.jupiter.api.Assertions.assertEquals;

public abstract class E2ETest {
    private final ByteArrayOutputStream proxyOutput = new ByteArrayOutputStream();
    private final Context jsContext = Context.newBuilder("js")
            .allowAllAccess(true)
            .out(proxyOutput)
            .build();

    protected final void assertResourceScriptOutputs(String expectedOutput, String resource) {
        InputStream stream = this.getClass().getResourceAsStream("/e2e/" + resource + ".sn");
        String script = new BufferedReader(new InputStreamReader(stream)).lines().collect(joining("\n"));

        assertScriptOutputs(expectedOutput, script);
    }

    private void assertScriptOutputs(String expectedOutput, String literalScript) {
        String output = executeScript(literalScript);
        assertEquals(expectedOutput.trim(), output.trim().replaceAll("\\n{2,}", "\n"));
    }

    private String executeScript(String literalScript) {
        String compiledVersion = compileToString(literalScript);
        System.out.println(">> Source Code:\n" + literalScript);
        System.out.println(">> JavaScript:\n" + compiledVersion);

        proxyOutput.reset();
        jsContext.eval("js", compiledVersion);
        return new String(proxyOutput.toByteArray());
    }

    private String compileToString(String literalScript) {
        Source literalSource = Source.fromLiteral(literalScript);
        return Sonata.compile(CompilerLog.console(), Flowable.just(literalSource), JSBackend::new).map(String::new).blockingGet();
    }
}
