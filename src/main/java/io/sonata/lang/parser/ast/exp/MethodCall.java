package io.sonata.lang.parser.ast.exp;

import io.sonata.lang.parser.ast.Node;
import io.sonata.lang.source.SourcePosition;

import java.util.List;
import java.util.stream.Collectors;

public class MethodCall extends ComposedExpression implements Expression {
    private final Expression receiver;
    private final String method;
    private final List<Expression> arguments;

    public MethodCall(Expression receiver, String method, List<Expression> arguments) {
        this.receiver = receiver;
        this.method = method;
        this.arguments = arguments;
    }

    @Override
    public String representation() {
        return receiver.representation() + "." + method + "(" + arguments.stream().map(Node::representation).collect(Collectors.joining(", ")) + ")";
    }

    @Override
    public SourcePosition definition() {
        return receiver.definition();
    }
}
