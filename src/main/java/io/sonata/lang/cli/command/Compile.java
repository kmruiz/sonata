package io.sonata.lang.cli.command;

import io.reactivex.Flowable;
import io.sonata.lang.backend.js.JavaScriptBackend;
import io.sonata.lang.cli.Sonata;
import io.sonata.lang.log.CompilerLog;
import io.sonata.lang.source.Source;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;

public class Compile {
    public static void execute(List<String> files, String output) throws Exception {
        CompilerLog log = CompilerLog.console();
        Instant startingTime = Instant.now();

        Flowable<Source> sources = Flowable.fromIterable(files)
                .map(Paths::get)
                .map(Source::fromPath);

        try(
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(baos))
        ) {
            Sonata.compile(log, sources, new JavaScriptBackend(writer)).blockingAwait();
            Files.write(Paths.get(output), baos.toByteArray());
        } catch (NoSuchElementException e) {
            log.info("Could not compile because there are compilation errors.");
        }

        Instant endingTime = Instant.now();
        String duration = Duration.between(startingTime, endingTime).toString();
        log.info("Finished in " + duration);
    }
}
