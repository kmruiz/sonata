package io.sonata.lang.analyzer;

import io.reactivex.Flowable;
import io.reactivex.Single;
import io.sonata.lang.parser.ast.Node;

import java.util.Arrays;
import java.util.List;

public class Analyzer {
    private final List<Processor> processors;

    public Analyzer(Processor... processors) {
        this.processors = Arrays.asList(processors);
    }

    private static Node applyProcessor(Node current, Processor processor) {
        return processor.apply(current);
    }

    public Single<Node> apply(Node node) {
        return Flowable.fromIterable(processors).reduce(node, Analyzer::applyProcessor);
    }
}
