package io.sonata.lang.parser.ast.exp;

import io.sonata.lang.parser.ast.Node;
import io.sonata.lang.parser.ast.let.fn.SimpleParameter;

import java.util.List;
import java.util.stream.Collectors;

public class Lambda extends ComposedExpression {
    public final List<SimpleParameter> parameters;
    public final Expression body;

    public Lambda(List<SimpleParameter> parameters, Expression body) {
        this.parameters = parameters;
        this.body = body;
    }

    public static Lambda synthetic(List<SimpleParameter> parameters, Expression body) {
        return new Lambda(parameters, body);
    }

    @Override
    public String representation() {
        return "(" + parameters.stream().map(Node::representation).collect(Collectors.joining(",")) + ") => " + body.representation();
    }
}
