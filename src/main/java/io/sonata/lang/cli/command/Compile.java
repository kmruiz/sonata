package io.sonata.lang.cli.command;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.subjects.ReplaySubject;
import io.reactivex.subjects.Subject;
import io.sonata.lang.backend.BackendVisitor;
import io.sonata.lang.backend.js.JSBackend;
import io.sonata.lang.parser.Parser;
import io.sonata.lang.parser.ast.RequiresNodeNotifier;
import io.sonata.lang.parser.ast.RxRequiresNodeNotifier;
import io.sonata.lang.source.Source;
import io.sonata.lang.tokenizer.Tokenizer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

public class Compile {
    public static void execute(List<String> files, String output) {
        BackendVisitor visitor = new BackendVisitor(JSBackend::new);
        Tokenizer tokenizer = new Tokenizer();

        Subject<Source> requires = ReplaySubject.create();
        RequiresNodeNotifier notifier = new RxRequiresNodeNotifier(requires);

        Flowable.fromIterable(files)
                .map(Paths::get)
                .map(Source::fromPath)
                .concatWith(Single.fromSupplier(Source::endOfProgram))
                .forEach(requires::onNext);

        var bytes = requires.toFlowable(BackpressureStrategy.BUFFER)
                .flatMap(Source::read)
                .flatMap(tokenizer::process)
                .reduce(Parser.initial(notifier), Parser::reduce)
                .map(visitor::generateSourceCode)
                .blockingGet();

        try {
            Files.write(Paths.get(output), bytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
