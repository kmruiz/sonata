package io.sonata.lang.parser.ast.exp;

import io.sonata.lang.tokenizer.token.SeparatorToken;
import io.sonata.lang.tokenizer.token.Token;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static io.sonata.lang.javaext.Lists.append;

public class PartialArray implements Expression {
    public final List<Expression> expressions;
    public final Expression currentExpression;

    private PartialArray(List<Expression> expressions, Expression currentExpression) {
        this.expressions = expressions;
        this.currentExpression = currentExpression;
    }

    public static PartialArray initial() {
        return new PartialArray(Collections.emptyList(), EmptyExpression.instance());
    }

    @Override
    public Expression consume(Token token) {
        var next = currentExpression.consume(token);
        if (next == null) {
            if (token instanceof SeparatorToken) {
                var sep = (SeparatorToken) token;
                if (sep.separator.equals(",")) {
                    return new PartialArray(append(expressions, currentExpression), EmptyExpression.instance());
                }

                if (sep.separator.equals("]")) {
                    if (currentExpression instanceof EmptyExpression) {
                        return new LiteralArray(expressions);
                    }

                    return new LiteralArray(append(expressions, currentExpression));
                }
            }

            return null;
        }

        return new PartialArray(expressions, next);
    }

    @Override
    public String representation() {
        return "[" + expressions.stream().map(Expression::representation).collect(Collectors.joining(", ")) + ", " + currentExpression.representation();
    }
}
