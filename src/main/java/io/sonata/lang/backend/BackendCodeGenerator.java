package io.sonata.lang.backend;

import io.reactivex.Single;
import io.sonata.lang.parser.ast.Node;

public interface BackendCodeGenerator {
    Single<byte[]> generateFor(Node node);
}
