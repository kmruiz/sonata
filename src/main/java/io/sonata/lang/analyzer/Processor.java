package io.sonata.lang.analyzer;

import io.sonata.lang.parser.ast.Node;

public interface Processor {
    String phase();
    Node apply(Node node);
}
