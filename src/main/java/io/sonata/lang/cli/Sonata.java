package io.sonata.lang.cli;

import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.subjects.ReplaySubject;
import io.reactivex.subjects.Subject;
import io.sonata.lang.analyzer.Analyzer;
import io.sonata.lang.analyzer.destructuring.DestructuringProcessor;
import io.sonata.lang.log.CompilerLog;
import io.sonata.lang.analyzer.partials.QuestionMarkPartialFunctionProcessor;
import io.sonata.lang.analyzer.symbols.SymbolMap;
import io.sonata.lang.analyzer.typeSystem.ClassScopeProcessor;
import io.sonata.lang.analyzer.typeSystem.Scope;
import io.sonata.lang.backend.BackendVisitor;
import io.sonata.lang.parser.Parser;
import io.sonata.lang.parser.ast.RequiresNodeNotifier;
import io.sonata.lang.parser.ast.RxRequiresNodeNotifier;
import io.sonata.lang.source.Source;
import io.sonata.lang.tokenizer.Tokenizer;

import java.util.HashMap;

import static io.reactivex.BackpressureStrategy.BUFFER;

public class Sonata {
    public static Single<byte[]> compile(Flowable<Source> sources, BackendVisitor.BackendFactory backend) {
        CompilerLog log = CompilerLog.console();
        SymbolMap symbolMap = new SymbolMap(new HashMap<>());
        BackendVisitor visitor = new BackendVisitor(backend);
        Tokenizer tokenizer = new Tokenizer();
        Analyzer analyzer = new Analyzer(
                log, symbolMap,
                new DestructuringProcessor(symbolMap),
                new QuestionMarkPartialFunctionProcessor(),
                new ClassScopeProcessor(log, Scope.root())
        );

        Subject<Source> requires = ReplaySubject.create();
        RequiresNodeNotifier notifier = new RxRequiresNodeNotifier(requires);

        return sources
                .concatWith(Single.fromSupplier(Source::endOfProgram))
                .concatWith(requires.toFlowable(BUFFER))
                .flatMap(Source::read)
                .flatMap(tokenizer::process)
                .reduce(Parser.initial(notifier), Parser::reduce)
                .toFlowable()
                .flatMap(analyzer::apply)
                .flatMap(visitor::generateSourceCode)
                .firstElement()
                .toSingle();
    }
}
