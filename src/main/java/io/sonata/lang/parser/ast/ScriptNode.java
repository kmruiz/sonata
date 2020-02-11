/*
 * Copyright (c) 2020 Kevin Mas Ruiz
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package io.sonata.lang.parser.ast;

import io.sonata.lang.exception.ParserException;
import io.sonata.lang.log.CompilerLog;
import io.sonata.lang.source.SourcePosition;
import io.sonata.lang.tokenizer.token.Token;

import java.util.List;
import java.util.stream.Collectors;

import static io.sonata.lang.javaext.Lists.append;
import static java.util.Collections.emptyList;

public class ScriptNode implements Node {
    public final CompilerLog log;
    public final List<Node> nodes;
    public final Node currentNode;

    public ScriptNode(CompilerLog log, List<Node> nodes, Node currentNode) {
        this.log = log;
        this.nodes = nodes;
        this.currentNode = currentNode;
    }

    public static ScriptNode initial(CompilerLog log) {
        return new ScriptNode(log, emptyList(), RootNode.instance());
    }

    @Override
    public String representation() {
        return nodes.stream().map(Node::representation).collect(Collectors.joining("\n"));
    }

    @Override
    public Node consume(Token token) {
        Node nextNode = null;
        try {
            if (currentNode == null) {
                throw new ParserException(nodes.get(nodes.size()-1), "Could not parse token before '" + token.representation() + " '. Please fill a bug with sample code.");
            }

            nextNode = currentNode.consume(token);
            if (nextNode == null) {
                return new ScriptNode(log, append(nodes, currentNode), RootNode.instance().consume(token));
            }
        } catch (ParserException e) {
            log.syntaxError(e.syntaxError());
            nextNode = new PanicNode(token.sourcePosition());
        }

        return new ScriptNode(log, nodes, nextNode);
    }

    @Override
    public SourcePosition definition() {
        return null;
    }
}
