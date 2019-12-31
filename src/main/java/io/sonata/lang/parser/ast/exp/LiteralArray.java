package io.sonata.lang.parser.ast.exp;

import io.sonata.lang.source.SourcePosition;

import java.util.List;
import java.util.stream.Collectors;

public class LiteralArray extends ComposedExpression implements Expression {
    public final SourcePosition definition;
    public final List<Expression> expressions;

    public LiteralArray(SourcePosition definition, List<Expression> expressions) {
        this.definition = definition;
        this.expressions = expressions;
    }

    @Override
    public String representation() {
        return "[" + expressions.stream().map(Expression::representation).collect(Collectors.joining(", ")) + "]";
    }

    @Override
    public SourcePosition definition() {
        return definition;
    }
}
