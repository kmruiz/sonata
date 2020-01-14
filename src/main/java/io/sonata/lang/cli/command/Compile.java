/*
 * Copyright (c) 2020 Kevin Mas Ruiz
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
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
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.NoSuchElementException;

public class Compile {
    public static void execute(List<String> files, String output) throws Exception {
        CompilerLog log = CompilerLog.console();
        Instant startingTime = Instant.now().truncatedTo(ChronoUnit.MILLIS);

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

        Instant endingTime = Instant.now().truncatedTo(ChronoUnit.MILLIS);
        String duration = humanReadableFormat(Duration.between(startingTime, endingTime));
        log.info("Finished in " + duration);
    }

    private static String humanReadableFormat(Duration duration) {
        return duration.toString()
                .substring(2)
                .replaceAll("(\\d[HMS])(?!$)", "$1 ")
                .toLowerCase();
    }
}
