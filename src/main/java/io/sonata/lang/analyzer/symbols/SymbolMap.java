package io.sonata.lang.analyzer.symbols;

import io.sonata.lang.analyzer.Processor;
import io.sonata.lang.parser.ast.Node;
import io.sonata.lang.parser.ast.ScriptNode;
import io.sonata.lang.parser.ast.classes.values.ValueClass;

import java.util.Map;
import java.util.NoSuchElementException;

public class SymbolMap implements Processor, SymbolResolver {
    private final Map<String, SymbolDeclaration> dictionary;

    public SymbolMap(Map<String, SymbolDeclaration> dictionary) {
        this.dictionary = dictionary;
    }

    @Override
    public Node apply(Node node) {
        if (node instanceof ScriptNode) {
            ((ScriptNode) node).nodes.forEach(this::apply);
        } else if (node instanceof ValueClass) {
            registerValueClass((ValueClass) node);
        }

        return node;
    }

    @Override
    public <T extends Node> T resolve(String symbol, Class<T> nodeClass) {
        if (!isSymbolOfType(symbol, nodeClass)) {
            throw new NoSuchElementException(symbol + " of type " + nodeClass.getSimpleName());
        }

        return (T) dictionary.get(symbol).node;
    }

    @Override
    public boolean isSymbolOfType(String symbol, Class<? extends Node> nodeClass) {
        var declaration = dictionary.get(symbol);
        if (declaration == null) {
            return false;
        }

        return nodeClass.isAssignableFrom(declaration.node.getClass());
    }

    private void registerValueClass(ValueClass node) {
        dictionary.put(node.name, new SymbolDeclaration(node.name, node));
    }
}
