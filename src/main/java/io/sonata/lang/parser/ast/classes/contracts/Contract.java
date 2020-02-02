/*
 * Copyright (c) 2020 Kevin Mas Ruiz
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package io.sonata.lang.parser.ast.classes.contracts;

import io.sonata.lang.parser.ast.Node;
import io.sonata.lang.parser.ast.Scoped;
import io.sonata.lang.source.SourcePosition;
import io.sonata.lang.tokenizer.token.Token;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Contract implements Node, Scoped {
    public final SourcePosition definition;
    public final String name;
    public final List<Node> body;

    public Contract(SourcePosition definition, String name) {
        this(definition, name, Collections.emptyList());
    }

    public Contract(SourcePosition definition, String name, List<Node> body) {
        this.definition = definition;
        this.name = name;
        this.body = body;
    }

    @Override
    public Node consume(Token token) {
        if (token.representation().equals("{")) {
            return PartialContractWithBody.initial(definition, name);
        }

        return null;
    }

    @Override
    public String representation() {
        return String.format("contract %s {\n\t%s\n}",
                name,
                body.stream().map(Node::representation).collect(Collectors.joining("\n\t"))
        );
    }

    @Override
    public SourcePosition definition() {
        return definition;
    }

    @Override
    public String scopeId() {
        return "contract " + name;
    }
}
