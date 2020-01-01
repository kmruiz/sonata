package io.sonata.lang.parser.ast.type;

import io.sonata.lang.parser.ast.Node;
import io.sonata.lang.tokenizer.token.Token;

public interface ASTType extends Node {
    @Override
    ASTType consume(Token token);
}
