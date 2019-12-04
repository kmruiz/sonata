package io.sonata.lang.backend;

import io.reactivex.Flowable;
import io.sonata.lang.parser.ast.Node;

public interface BackendCodeGenerator {
    Flowable<byte[]> generateFor(Node node);
}
