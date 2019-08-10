package io.sonata.lang.analyzer;

import io.reactivex.Flowable;
import io.sonata.lang.parser.ast.Node;

import java.util.Arrays;
import java.util.List;

public class Analyzer {
    private final List<Processor> processors;

    public Analyzer(Processor... processors) {
        this.processors = Arrays.asList(processors);
    }

    public Node apply(Node node) {
        return Flowable.fromIterable(processors)
                .reduce(node, (current, processor) -> processor.apply(current))
                .blockingGet();
    }
}
