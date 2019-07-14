package io.sonata.lang.parser.ast;

import io.sonata.lang.tokenizer.token.Token;

import java.util.List;
import java.util.stream.Collectors;

import static io.sonata.lang.javaext.Lists.append;
import static java.util.Collections.emptyList;

public class ScriptNode implements Node {
    public final List<Node> nodes;
    public final Node currentNode;

    private ScriptNode(List<Node> nodes, Node currentNode) {
        this.nodes = nodes;
        this.currentNode = currentNode;
    }

    public static ScriptNode initial() {
        return new ScriptNode(emptyList(), RootNode.instance());
    }

    @Override
    public String representation() {
        return nodes.stream().map(Node::representation).collect(Collectors.joining("\n"));
    }

    @Override
    public Node consume(Token token) {
        Node nextNode = currentNode.consume(token);
        if (nextNode == null) {
            return new ScriptNode(append(nodes, currentNode), RootNode.instance().consume(token));
        }

        return new ScriptNode(nodes, nextNode);
    }
}
