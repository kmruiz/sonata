package io.sonata.lang.parser.ast.exp;

import io.sonata.lang.parser.ast.Node;

import java.util.List;
import java.util.stream.Collectors;

public class FunctionCall extends ComposedExpression implements Expression {
    public final Expression receiver;
    public final List<Expression> arguments;

    public FunctionCall(Expression receiver, List<Expression> arguments) {
        this.receiver = receiver;
        this.arguments = arguments;
    }

    @Override
    public String representation() {
        return receiver.representation() + "(" + arguments.stream().map(Node::representation).collect(Collectors.joining(", ")) + ")";
    }
}
