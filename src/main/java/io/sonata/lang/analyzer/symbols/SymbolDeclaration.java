package io.sonata.lang.analyzer.symbols;

import io.sonata.lang.parser.ast.Node;

public class SymbolDeclaration {
    public final String name;
    public final Node node;

    public SymbolDeclaration(String name, Node node) {
        this.name = name;
        this.node = node;
    }
}
