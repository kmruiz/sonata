package io.sonata.lang.parser.ast.exp;

import io.sonata.lang.parser.ast.Node;
import io.sonata.lang.tokenizer.token.Token;

public interface Expression extends Node {
    @Override
    Expression consume(Token token);
}
