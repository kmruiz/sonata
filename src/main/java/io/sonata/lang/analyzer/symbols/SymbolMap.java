package io.sonata.lang.analyzer.symbols;

import io.sonata.lang.analyzer.Processor;
import io.sonata.lang.parser.ast.Node;
import io.sonata.lang.parser.ast.ScriptNode;
import io.sonata.lang.parser.ast.classes.values.ValueClass;
import io.sonata.lang.parser.ast.let.LetFunction;

import java.util.Map;

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
    public boolean isValueClass(String symbol) {
        return isSymbolRegisteredWithType(symbol, ValueClass.class);
    }

    private boolean isSymbolRegisteredWithType(String symbol, Class<? extends Node> nodeClass) {
        var declaration = dictionary.get(symbol);
        if (declaration == null) {
            return false;
        }

        return nodeClass.isAssignableFrom(declaration.node.getClass());
    }

    private SymbolDeclaration registerValueClass(ValueClass node) {
        return dictionary.put(node.name, new SymbolDeclaration(node.name, node));
    }
}
