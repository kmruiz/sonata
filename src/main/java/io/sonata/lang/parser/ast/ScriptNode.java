package io.sonata.lang.parser.ast;

import io.sonata.lang.parser.ast.requires.RequiresNode;
import io.sonata.lang.source.SourcePosition;
import io.sonata.lang.tokenizer.token.Token;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static io.sonata.lang.javaext.Lists.append;
import static java.util.Collections.emptyList;

public class ScriptNode implements Node {
    public final List<Node> nodes;
    public final Node currentNode;
    public final RequiresNodeNotifier requiresNotifier;

    public ScriptNode(List<Node> nodes, Node currentNode, RequiresNodeNotifier requiresNotifier) {
        this.nodes = nodes;
        this.currentNode = currentNode;
        this.requiresNotifier = requiresNotifier;
    }

    public static ScriptNode initial(RequiresNodeNotifier notifier) {
        return new ScriptNode(emptyList(), RootNode.instance(), notifier);
    }

    @Override
    public String representation() {
        return nodes.stream().map(Node::representation).collect(Collectors.joining("\n"));
    }

    @Override
    public Node consume(Token token) {
        if (token.representation().equals("\0")) {
            requiresNotifier.done();
            return this;
        }

        Node nextNode = currentNode.consume(token);
        if (nextNode == null) {
            return new ScriptNode(append(nodes, currentNode), RootNode.instance().consume(token), requiresNotifier);
        }

        if (nextNode instanceof RequiresNode) {
            try {
                requiresNotifier.moduleRequired(null, ((RequiresNode) nextNode).module);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }

        return new ScriptNode(nodes, nextNode, requiresNotifier);
    }

    @Override
    public SourcePosition definition() {
        return null;
    }
}
