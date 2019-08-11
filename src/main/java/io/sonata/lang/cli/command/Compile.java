package io.sonata.lang.cli.command;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.subjects.ReplaySubject;
import io.reactivex.subjects.Subject;
import io.sonata.lang.analyzer.Analyzer;
import io.sonata.lang.analyzer.destructuring.DestructuringProcessor;
import io.sonata.lang.analyzer.symbols.SymbolMap;
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
import java.util.HashMap;
import java.util.List;

public class Compile {
    public static void execute(List<String> files, String output) {
        SymbolMap symbolMap = new SymbolMap(new HashMap<>());
        BackendVisitor visitor = new BackendVisitor(JSBackend::new);
        Tokenizer tokenizer = new Tokenizer();
        Analyzer analyzer = new Analyzer(
                symbolMap,
                new DestructuringProcessor(symbolMap)
        );

        Subject<Source> requires = ReplaySubject.create();
        RequiresNodeNotifier notifier = new RxRequiresNodeNotifier(requires);

        try {
            Flowable.fromIterable(files)
                    .map(Paths::get)
                    .map(Source::fromPath)
                    .concatWith(Single.fromSupplier(Source::endOfProgram))
                    .forEach(requires::onNext);

            var bytes = requires.toFlowable(BackpressureStrategy.BUFFER)
                    .flatMap(Source::read)
                    .flatMap(tokenizer::process)
                    .reduce(Parser.initial(notifier), Parser::reduce)
                    .map(analyzer::apply)
                    .map(visitor::generateSourceCode)
                    .blockingGet();

            Files.write(Paths.get(output), bytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
