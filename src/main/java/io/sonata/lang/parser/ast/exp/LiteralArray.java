package io.sonata.lang.parser.ast.exp;

import java.util.List;
import java.util.stream.Collectors;

public class LiteralArray extends ComposedExpression implements Expression {
    public final List<Expression> expressions;

    public LiteralArray(List<Expression> expressions) {

        this.expressions = expressions;
    }

    @Override
    public String representation() {
        return "[" + expressions.stream().map(Expression::representation).collect(Collectors.joining(", ")) + "]";
    }
}
