/*
 * Copyright (c) 2020 Kevin Mas Ruiz
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package io.sonata.lang.analyzer.symbols;

import io.sonata.lang.analyzer.Processor;
import io.sonata.lang.parser.ast.Node;
import io.sonata.lang.parser.ast.ScriptNode;
import io.sonata.lang.parser.ast.classes.values.ValueClass;

import java.util.Map;
import java.util.NoSuchElementException;

public final class SymbolMap implements Processor, SymbolResolver {
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
    @SuppressWarnings("unchecked")
    public <T extends Node> T resolve(String symbol, Class<T> nodeClass) {
        if (!isSymbolOfType(symbol, nodeClass)) {
            throw new NoSuchElementException(symbol + " of type " + nodeClass.getSimpleName());
        }

        return (T) dictionary.get(symbol).node;
    }

    @Override
    public boolean isSymbolOfType(String symbol, Class<? extends Node> nodeClass) {
        SymbolDeclaration declaration = dictionary.get(symbol);
        if (declaration == null) {
            return false;
        }

        return nodeClass.isAssignableFrom(declaration.node.getClass());
    }

    private void registerValueClass(ValueClass node) {
        dictionary.put(node.name, new SymbolDeclaration(node.name, node));
    }

    @Override
    public String phase() {
        return "SYMBOL MAPPING";
    }
}
