package io.sonata.lang.parser.ast.classes.fields;

import io.sonata.lang.parser.ast.Node;
import io.sonata.lang.tokenizer.token.Token;

public interface Field extends Node {
    boolean isDone();

    String name();

    @Override
    Field consume(Token token);
}
