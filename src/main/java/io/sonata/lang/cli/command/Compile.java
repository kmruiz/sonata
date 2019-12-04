package io.sonata.lang.cli.command;

import io.reactivex.Flowable;
import io.sonata.lang.backend.js.JSBackend;
import io.sonata.lang.cli.Sonata;
import io.sonata.lang.source.Source;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

public class Compile {
    public static void execute(List<String> files, String output) throws Exception {
        Instant startingTime = Instant.now();

        Flowable<Source> sources = Flowable.fromIterable(files)
                .map(Paths::get)
                .map(Source::fromPath);

        byte[] result = Sonata.compile(sources, JSBackend::new).blockingGet();
        Files.write(Paths.get(output), result);

        Instant endingTime = Instant.now();
        String duration = Duration.between(startingTime, endingTime).toString();
        System.out.println("Compiled everything in " + duration);
    }
}
