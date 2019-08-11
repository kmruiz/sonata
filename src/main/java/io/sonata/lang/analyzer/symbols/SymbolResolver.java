package io.sonata.lang.analyzer.symbols;

import io.sonata.lang.parser.ast.Node;

public interface SymbolResolver {
    <T extends Node> T resolve(String symbol, Class<T> nodeClass);
    boolean isSymbolOfType(String symbol, Class<? extends Node> nodeClass);
}
