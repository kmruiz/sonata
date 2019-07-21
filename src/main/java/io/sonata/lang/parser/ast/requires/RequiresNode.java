package io.sonata.lang.parser.ast.requires;

import io.sonata.lang.parser.ast.Node;
import io.sonata.lang.tokenizer.token.Token;

public class RequiresNode implements Node {
    public final String module;

    public RequiresNode(String module) {
        this.module = module;
    }

    @Override
    public String representation() {
        return "requires " + module;
    }

    @Override
    public Node consume(Token token) {
        return null;
    }
}
