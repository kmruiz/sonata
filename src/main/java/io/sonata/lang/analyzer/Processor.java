package io.sonata.lang.analyzer;

import io.sonata.lang.parser.ast.Node;

public interface Processor {
    Node apply(Node node);
}
