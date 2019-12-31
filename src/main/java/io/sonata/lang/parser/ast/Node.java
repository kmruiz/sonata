package io.sonata.lang.parser.ast;

import io.sonata.lang.source.SourcePosition;
import io.sonata.lang.tokenizer.token.Token;

public interface Node {
    SourcePosition definition();
    String representation();
    Node consume(Token token);
}
