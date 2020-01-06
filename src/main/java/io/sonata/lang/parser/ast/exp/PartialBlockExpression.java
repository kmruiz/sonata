/*
 * Copyright (c) 2020 Kevin Mas Ruiz
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package io.sonata.lang.parser.ast.exp;

import io.sonata.lang.exception.ParserException;
import io.sonata.lang.source.SourcePosition;
import io.sonata.lang.tokenizer.token.Token;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static io.sonata.lang.javaext.Lists.append;

public class PartialBlockExpression implements Expression {
    public final SourcePosition definition;
    public final List<Expression> nodes;
    public final Expression currentNode;

    private PartialBlockExpression(SourcePosition definition, List<Expression> nodes, Expression currentNode) {
        this.definition = definition;
        this.nodes = nodes;
        this.currentNode = currentNode;
    }

    public static PartialBlockExpression initial(SourcePosition definition) {
        return new PartialBlockExpression(definition, Collections.emptyList(), EmptyExpression.instance());
    }

    @Override
    public Expression consume(Token token) {
        if (token.representation().equals(":")) {
            if (this.currentNode instanceof Atom && nodes.isEmpty()) {
                final Atom atom = (Atom) this.currentNode;
                if (atom.type == Atom.Type.IDENTIFIER) {
                    return PartialRecord.waitingValue(definition, atom);
                }

                throw new ParserException(this.currentNode, "Record keys can only be identifiers, but got '" + token.representation() + "'");
            }
        }

        Expression nextNode = currentNode.consume(token);
        if (nextNode == null) {
            if (token.representation().equals("}")) {
                if (currentNode instanceof EmptyExpression) {
                    if (nodes.isEmpty()) {
                        return new Record(definition, new HashMap<>());
                    }

                    return new BlockExpression(definition, nodes);
                }

                return new BlockExpression(definition, append(nodes, currentNode));
            }

            if (!(currentNode instanceof EmptyExpression)) {
                return new PartialBlockExpression(definition, append(nodes, currentNode), EmptyExpression.instance()).consume(token);
            }

            return this;
        }

        return new PartialBlockExpression(definition, nodes, nextNode);
    }

    @Override
    public String representation() {
        return null;
    }

    @Override
    public SourcePosition definition() {
        return definition;
    }
}
