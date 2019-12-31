package io.sonata.lang.parser.ast.exp;

import io.sonata.lang.parser.ast.Node;
import io.sonata.lang.parser.ast.let.fn.SimpleParameter;
import io.sonata.lang.source.SourcePosition;

import java.util.List;
import java.util.stream.Collectors;

public class Lambda extends ComposedExpression {
    public final SourcePosition definition;
    public final List<SimpleParameter> parameters;
    public final Expression body;

    public Lambda(SourcePosition definition, List<SimpleParameter> parameters, Expression body) {
        this.definition = definition;
        this.parameters = parameters;
        this.body = body;
    }

    public static Lambda synthetic(SourcePosition definition, List<SimpleParameter> parameters, Expression body) {
        return new Lambda(definition, parameters, body);
    }

    @Override
    public String representation() {
        return "(" + parameters.stream().map(Node::representation).collect(Collectors.joining(",")) + ") => " + body.representation();
    }

    @Override
    public SourcePosition definition() {
        return definition;
    }
}
