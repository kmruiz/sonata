package io.sonata.lang.cli.command;

import io.reactivex.Flowable;
import io.sonata.lang.backend.js.JSBackend;
import io.sonata.lang.cli.Sonata;
import io.sonata.lang.log.CompilerLog;
import io.sonata.lang.source.Source;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

public class Compile {
    public static void execute(List<String> files, String output) throws Exception {
        CompilerLog log = CompilerLog.console();
        Instant startingTime = Instant.now();

        Flowable<Source> sources = Flowable.fromIterable(files)
                .map(Paths::get)
                .map(Source::fromPath);

        byte[] result;

        try {
            result = Sonata.compile(log, sources, JSBackend::new).blockingGet();
            Files.write(Paths.get(output), result);
        } catch (Exception e) {
            log.info("Could not compile because there have been compilation errors. Please check the log.");
        }

        Instant endingTime = Instant.now();
        String duration = Duration.between(startingTime, endingTime).toString();
        log.info("Finished in " + duration);
    }
}
