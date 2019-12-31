package io.sonata.lang.parser.ast.exp;

import io.sonata.lang.source.SourcePosition;
import io.sonata.lang.tokenizer.token.SeparatorToken;
import io.sonata.lang.tokenizer.token.Token;

import java.util.Collections;
import java.util.List;

import static io.sonata.lang.javaext.Lists.append;
import static io.sonata.lang.javaext.Objects.requireNonNullElse;

public class PartialFunctionCall implements Expression {
    public final SourcePosition definition;
    public final Expression receiver;
    public final List<Expression> arguments;
    public final Expression currentExpression;

    private PartialFunctionCall(SourcePosition definition, Expression receiver, List<Expression> arguments, Expression currentExpression) {
        this.definition = definition;
        this.receiver = receiver;
        this.arguments = arguments;
        this.currentExpression = currentExpression;
    }

    public static PartialFunctionCall on(SourcePosition definition, Expression receiver) {
        return new PartialFunctionCall(definition, receiver, Collections.emptyList(), EmptyExpression.instance());
    }

    @Override
    public Expression consume(Token token) {
        Expression next = currentExpression.consume(token);
        if (next == null || next instanceof Lambda) {
            if (token instanceof SeparatorToken) {
                SeparatorToken sep = (SeparatorToken) token;
                if (sep.separator.equals(",")) {
                    return new PartialFunctionCall(definition, receiver, append(arguments, requireNonNullElse(next, currentExpression)), EmptyExpression.instance());
                }

                if (sep.separator.equals(")")) {
                    return new FunctionCall(receiver, append(arguments, requireNonNullElse(next, currentExpression)));
                }
            }
        }

        return new PartialFunctionCall(definition, receiver, arguments, next);
    }

    @Override
    public String representation() {
        return "";
    }

    @Override
    public SourcePosition definition() {
        return definition;
    }
}
