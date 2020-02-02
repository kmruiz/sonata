/*
 * Copyright (c) 2020 Kevin Mas Ruiz
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package io.sonata.lang.parser.ast.classes.contracts;

import io.sonata.lang.parser.ast.Node;
import io.sonata.lang.parser.ast.RootNode;
import io.sonata.lang.source.SourcePosition;
import io.sonata.lang.tokenizer.token.Token;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static io.sonata.lang.javaext.Lists.append;

public class PartialContractWithBody implements Node {
    private final SourcePosition definition;
    private final String name;
    private final List<Node> declarations;
    private final Node current;

    private PartialContractWithBody(SourcePosition definition, String name, List<Node> declarations, Node current) {
        this.definition = definition;
        this.name = name;
        this.declarations = declarations;
        this.current = current;
    }

    public static PartialContractWithBody initial(SourcePosition definition, String name) {
        return new PartialContractWithBody(definition, name, Collections.emptyList(), RootNode.instance());
    }

    @Override
    public String representation() {
        return String.format("contract %s {\n\t%s\n}",
                name,
                declarations.stream().map(Node::representation).collect(Collectors.joining("\n\t"))
        );
    }

    @Override
    public Node consume(Token token) {
        Node nextExpr = current.consume(token);
        if (nextExpr == null) {
            if (token.representation().equals("}")) {
                if (current instanceof RootNode) {
                    return new Contract(definition, name, declarations);
                }

                return new Contract(definition, name, append(declarations, current));
            }

            if (current instanceof RootNode) {
                return this;
            }

            return new PartialContractWithBody(definition, name, append(declarations, current), RootNode.instance().consume(token));
        }

        return new PartialContractWithBody(definition, name, declarations, nextExpr);
    }

    @Override
    public SourcePosition definition() {
        return definition;
    }
}
