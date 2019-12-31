package io.sonata.lang.parser.ast.requires;

import io.sonata.lang.parser.ast.Node;
import io.sonata.lang.source.SourcePosition;
import io.sonata.lang.tokenizer.token.Token;

public class RequiresNode implements Node {
    private final SourcePosition definition;
    public final String module;

    public RequiresNode(SourcePosition definition, String module) {
        this.definition = definition;
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

    @Override
    public SourcePosition definition() {
        return definition;
    }
}
