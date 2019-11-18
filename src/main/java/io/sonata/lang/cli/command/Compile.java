package io.sonata.lang.cli.command;

import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.subjects.ReplaySubject;
import io.reactivex.subjects.Subject;
import io.sonata.lang.analyzer.Analyzer;
import io.sonata.lang.analyzer.destructuring.DestructuringProcessor;
import io.sonata.lang.analyzer.partials.QuestionMarkPartialFunctionProcessor;
import io.sonata.lang.analyzer.symbols.SymbolMap;
import io.sonata.lang.backend.BackendVisitor;
import io.sonata.lang.backend.js.JSBackend;
import io.sonata.lang.parser.Parser;
import io.sonata.lang.parser.ast.RequiresNodeNotifier;
import io.sonata.lang.parser.ast.RxRequiresNodeNotifier;
import io.sonata.lang.source.Source;
import io.sonata.lang.tokenizer.Tokenizer;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;

import static io.reactivex.BackpressureStrategy.BUFFER;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

public class Compile {
    public static void execute(List<String> files, String output) throws Exception {
        Instant startingTime = Instant.now();

        SymbolMap symbolMap = new SymbolMap(new HashMap<>());
        BackendVisitor visitor = new BackendVisitor(JSBackend::new);
        Tokenizer tokenizer = new Tokenizer();
        Analyzer analyzer = new Analyzer(
                symbolMap,
                new DestructuringProcessor(symbolMap),
                new QuestionMarkPartialFunctionProcessor()
        );

        Subject<Source> requires = ReplaySubject.create();
        RequiresNodeNotifier notifier = new RxRequiresNodeNotifier(requires);

        byte[] byteCode = Flowable.fromIterable(files)
                .map(Paths::get)
                .map(Source::fromPath)
                .concatWith(Single.fromSupplier(Source::endOfProgram))
                .concatWith(requires.toFlowable(BUFFER))
                .flatMap(Source::read)
                .flatMap(tokenizer::process)
                .reduce(Parser.initial(notifier), Parser::reduce)
                .flatMap(analyzer::apply)
                .flatMap(visitor::generateSourceCode)
                .blockingGet();

        Files.write(Paths.get(output), byteCode, CREATE, TRUNCATE_EXISTING);

        Instant endingTime = Instant.now();
        String duration = Duration.between(startingTime, endingTime).toString();
        System.out.println("Compiled everything in " + duration);
    }
}
