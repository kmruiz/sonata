package io.sonata.lang.backend;

import io.sonata.lang.parser.ast.Node;

public interface BackendCodeGenerator {
    byte[] generateFor(Node node);
}
