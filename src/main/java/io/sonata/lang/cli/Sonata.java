/*
 * Copyright (c) 2020 Kevin Mas Ruiz
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package io.sonata.lang.cli;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.sonata.lang.analyzer.Analyzer;
import io.sonata.lang.analyzer.continuations.AsyncFunctionProcessor;
import io.sonata.lang.analyzer.continuations.ContinuationProcessor;
import io.sonata.lang.analyzer.destructuring.DestructuringProcessor;
import io.sonata.lang.analyzer.fops.FunctionCompositionProcessor;
import io.sonata.lang.analyzer.partials.QuestionMarkPartialFunctionProcessor;
import io.sonata.lang.analyzer.requires.InlineRequiredModuleProcessor;
import io.sonata.lang.analyzer.symbols.SymbolMap;
import io.sonata.lang.analyzer.typeSystem.*;
import io.sonata.lang.backend.CompilerBackend;
import io.sonata.lang.log.CompilerLog;
import io.sonata.lang.parser.Parser;
import io.sonata.lang.parser.ast.RequiresPathResolver;
import io.sonata.lang.parser.ast.RequiresPaths;
import io.sonata.lang.parser.ast.ScriptNode;
import io.sonata.lang.source.Source;
import io.sonata.lang.tokenizer.Tokenizer;

import java.util.HashMap;

public class Sonata {
    public static Completable compile(CompilerLog log, Flowable<Source> sources, RequiresPaths requiresPaths, CompilerBackend backend) {
        SymbolMap symbolMap = new SymbolMap(new HashMap<>());
        Tokenizer tokenizer = new Tokenizer();
        Scope scope = Scope.root();
        Analyzer analyzer = new Analyzer(log,
                InlineRequiredModuleProcessor.processorInstance(scope, log, new RequiresPathResolver(requiresPaths, source -> parseScript(log, source))),
                symbolMap,
                QuestionMarkPartialFunctionProcessor.processorInstance(scope),
                ContractProcessor.processorInstance(scope, log),
                ClassScopeProcessor.processorInstance(scope, log),
                LetVariableProcessor.processorInstance(scope, log),
                TypeInferenceProcessor.processorInstance(scope),
                new ContractFulfillmentProcessor(log, scope),
                PropertyVisibilityProcessor.processorInstance(scope, log),
                ImmutabilityCheckProcessor.processorInstance(scope, log),
                EqualitySpecializationProcessor.processorInstance(scope, log),
                DestructuringProcessor.processorInstance(scope, symbolMap),
                FunctionCompositionProcessor.processorInstance(scope, log),
                ContinuationProcessor.processorInstance(scope),
                AsyncFunctionProcessor.processorInstance(scope)
        );

        return sources
                .flatMap(Source::read)
                .flatMap(tokenizer::process)
                .reduce(Parser.initial(log), Parser::reduce)
                .toFlowable()
                .flatMap(analyzer::apply)
                .doOnError(log::compilerError)
                .filter(e -> !log.hasErrors())
                .map(e -> (ScriptNode) e)
                .doOnNext(script -> backend.compile(scope, script))
                .firstElement()
                .toSingle()
                .ignoreElement();
    }

    private static ScriptNode parseScript(CompilerLog log, Source source) {
        Tokenizer tokenizer = new Tokenizer();

        return source.read()
                .flatMap(tokenizer::process)
                .reduce(Parser.initial(log), Parser::reduce)
                .cast(ScriptNode.class)
                .blockingGet();
    }
}
