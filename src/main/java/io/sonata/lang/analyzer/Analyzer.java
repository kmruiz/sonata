/*
 * Copyright (c) 2020 Kevin Mas Ruiz
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package io.sonata.lang.analyzer;

import io.reactivex.Flowable;
import io.sonata.lang.log.CompilerLog;
import io.sonata.lang.parser.ast.Node;

import java.util.Arrays;
import java.util.List;

public final class Analyzer {
    private final CompilerLog log;
    private final List<Processor> processors;

    public Analyzer(CompilerLog log, Processor... processors) {
        this.log = log;
        this.processors = Arrays.asList(processors);
    }

    private Node applyProcessor(Node current, Processor processor) {
        log.inPhase(processor.phase());
        return processor.apply(current);
    }

    public Flowable<Node> apply(Node node) {
        return Flowable.fromIterable(processors).reduce(node, this::applyProcessor).toFlowable();
    }
}
