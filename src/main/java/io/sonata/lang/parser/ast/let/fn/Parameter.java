package io.sonata.lang.parser.ast.let.fn;

import io.sonata.lang.parser.ast.Node;
import io.sonata.lang.tokenizer.token.Token;

public interface Parameter extends Node {
    boolean isDone();

    @Override
    Parameter consume(Token token);
}
