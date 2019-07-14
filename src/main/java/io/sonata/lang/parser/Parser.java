package io.sonata.lang.parser;

import io.sonata.lang.parser.ast.Node;
import io.sonata.lang.parser.ast.ScriptNode;
import io.sonata.lang.tokenizer.token.Token;

public final class Parser {
    public static Node initial() {
        return ScriptNode.initial();
    }

    public static Node reduce(Node current, Token token) {
        return current.consume(token);
    }
}
