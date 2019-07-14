package io.sonata.lang.cli.command;

import io.reactivex.Flowable;
import io.sonata.lang.backend.BackendVisitor;
import io.sonata.lang.backend.js.JSBackend;
import io.sonata.lang.parser.Parser;
import io.sonata.lang.source.Source;
import io.sonata.lang.tokenizer.Tokenizer;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;

public class Compile {
    public void execute(List<String> files, String output) {
        BackendVisitor visitor = new BackendVisitor(JSBackend::new);
        Tokenizer tokenizer = new Tokenizer();

        Flowable.fromIterable(files)
                .map(Paths::get)
                .map(Source::fromPath)
                .flatMap(Source::read)
                .flatMap(tokenizer::process)
                .reduce(Parser.initial(), Parser::reduce)
                .map(visitor::generateSourceCode)
                .subscribe(bytes -> Files.write(Paths.get(output), bytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING), Throwable::printStackTrace);
    }

    public static void main(String[] args) {
        new Compile().execute(Arrays.asList("samples/fibonacci/f.sn"), "samples/fibonacci/output.js");
    }
}
