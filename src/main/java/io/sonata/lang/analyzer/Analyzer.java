package io.sonata.lang.analyzer;

import io.reactivex.Flowable;
import io.sonata.lang.log.CompilerLog;
import io.sonata.lang.parser.ast.Node;

import java.util.Arrays;
import java.util.List;

public class Analyzer {
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
