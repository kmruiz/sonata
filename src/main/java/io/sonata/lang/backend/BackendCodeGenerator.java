package io.sonata.lang.backend;

import io.reactivex.Flowable;
import io.sonata.lang.parser.ast.exp.Expression;

public interface BackendCodeGenerator {
    Flowable<byte[]> generateFor(Expression node);
}
