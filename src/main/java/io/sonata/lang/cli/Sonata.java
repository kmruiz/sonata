/*
 * Copyright (c) 2020 Kevin Mas Ruiz
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package io.sonata.lang.cli;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.subjects.ReplaySubject;
import io.reactivex.subjects.Subject;
import io.sonata.lang.analyzer.Analyzer;
import io.sonata.lang.analyzer.destructuring.DestructuringProcessor;
import io.sonata.lang.analyzer.typeSystem.*;
import io.sonata.lang.backend.CompilerBackend;
import io.sonata.lang.log.CompilerLog;
import io.sonata.lang.analyzer.partials.QuestionMarkPartialFunctionProcessor;
import io.sonata.lang.analyzer.symbols.SymbolMap;
import io.sonata.lang.parser.Parser;
import io.sonata.lang.parser.ast.RequiresNodeNotifier;
import io.sonata.lang.parser.ast.RxRequiresNodeNotifier;
import io.sonata.lang.parser.ast.ScriptNode;
import io.sonata.lang.source.Source;
import io.sonata.lang.tokenizer.Tokenizer;

import java.util.HashMap;

import static io.reactivex.BackpressureStrategy.BUFFER;

public class Sonata {
    public static Completable compile(CompilerLog log, Flowable<Source> sources, CompilerBackend backend) {
        SymbolMap symbolMap = new SymbolMap(new HashMap<>());
        Tokenizer tokenizer = new Tokenizer();
        Scope scope = Scope.root();
        Analyzer analyzer = new Analyzer(log,
                symbolMap,
                new ClassScopeProcessor(log, scope),
                new TypeInferenceProcessor(log, scope),
                new LetVariableProcessor(log, scope),
                new ClassRelationshipValidator(log, scope),
                new DestructuringProcessor(symbolMap),
                new QuestionMarkPartialFunctionProcessor()
        );

        Subject<Source> requires = ReplaySubject.create();
        RequiresNodeNotifier notifier = new RxRequiresNodeNotifier(requires);

        return sources
                .concatWith(Single.fromSupplier(Source::endOfProgram))
                .concatWith(requires.distinct(s -> s.name).toFlowable(BUFFER))
                .flatMap(Source::read)
                .flatMap(tokenizer::process)
                .reduce(Parser.initial(log, notifier), Parser::reduce)
                .toFlowable()
                .flatMap(analyzer::apply)
                .filter(e -> !log.hasErrors())
                .map(e -> (ScriptNode) e)
                .doOnNext(backend::compile)
                .firstElement()
                .toSingle()
                .ignoreElement();
    }
}
