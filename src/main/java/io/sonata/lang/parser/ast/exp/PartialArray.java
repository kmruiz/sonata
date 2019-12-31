package io.sonata.lang.parser.ast.exp;

import io.sonata.lang.source.SourcePosition;
import io.sonata.lang.tokenizer.token.SeparatorToken;
import io.sonata.lang.tokenizer.token.Token;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static io.sonata.lang.javaext.Lists.append;

public class PartialArray implements Expression {
    public final SourcePosition definition;
    public final List<Expression> expressions;
    public final Expression currentExpression;

    private PartialArray(SourcePosition definition, List<Expression> expressions, Expression currentExpression) {
        this.definition = definition;
        this.expressions = expressions;
        this.currentExpression = currentExpression;
    }

    public static PartialArray initial(SourcePosition definition) {
        return new PartialArray(definition, Collections.emptyList(), EmptyExpression.instance());
    }

    @Override
    public Expression consume(Token token) {
        Expression next = currentExpression.consume(token);
        if (next == null) {
            if (token instanceof SeparatorToken) {
                SeparatorToken sep = (SeparatorToken) token;
                if (sep.separator.equals(",")) {
                    return new PartialArray(definition, append(expressions, currentExpression), EmptyExpression.instance());
                }

                if (sep.separator.equals("]")) {
                    if (currentExpression instanceof EmptyExpression) {
                        return new LiteralArray(definition, expressions);
                    }

                    return new LiteralArray(definition, append(expressions, currentExpression));
                }
            }

            return null;
        }

        return new PartialArray(definition, expressions, next);
    }

    @Override
    public String representation() {
        return "[" + expressions.stream().map(Expression::representation).collect(Collectors.joining(", ")) + ", " + currentExpression.representation();
    }

    @Override
    public SourcePosition definition() {
        return definition;
    }
}
