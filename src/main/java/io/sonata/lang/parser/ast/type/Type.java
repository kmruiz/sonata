package io.sonata.lang.parser.ast.type;

import io.sonata.lang.parser.ast.Node;
import io.sonata.lang.tokenizer.token.Token;

public interface Type extends Node {
    @Override
    Type consume(Token token);
}
