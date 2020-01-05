package io.sonata.lang.parser.ast;

import io.sonata.lang.exception.ParserException;
import io.sonata.lang.log.CompilerLog;
import io.sonata.lang.parser.ast.requires.RequiresNode;
import io.sonata.lang.source.SourcePosition;
import io.sonata.lang.tokenizer.token.Token;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static io.sonata.lang.javaext.Lists.append;
import static java.util.Collections.emptyList;

public class ScriptNode implements Node {
    public final CompilerLog log;
    public final List<Node> nodes;
    public final Node currentNode;
    public final RequiresNodeNotifier requiresNotifier;

    public ScriptNode(CompilerLog log, List<Node> nodes, Node currentNode, RequiresNodeNotifier requiresNotifier) {
        this.log = log;
        this.nodes = nodes;
        this.currentNode = currentNode;
        this.requiresNotifier = requiresNotifier;
    }

    public static ScriptNode initial(CompilerLog log, RequiresNodeNotifier notifier) {
        return new ScriptNode(log, emptyList(), RootNode.instance(), notifier);
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

        Node nextNode = null;
        try {
            nextNode = currentNode.consume(token);
            if (nextNode == null) {
                if (currentNode instanceof PanicNode) {
                    return new ScriptNode(log, nodes, RootNode.instance().consume(token), requiresNotifier);
                } else {
                    return new ScriptNode(log, append(nodes, currentNode), RootNode.instance().consume(token), requiresNotifier);
                }
            }
        } catch (ParserException e) {
            log.syntaxError(e.syntaxError());
            nextNode = new PanicNode(token.sourcePosition());
        }

        if (nextNode instanceof RequiresNode) {
            try {
                requiresNotifier.moduleRequired(null, ((RequiresNode) nextNode).module);
                return new ScriptNode(log, nodes, RootNode.instance(), requiresNotifier);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }

        return new ScriptNode(log, nodes, nextNode, requiresNotifier);
    }

    @Override
    public SourcePosition definition() {
        return null;
    }
}
