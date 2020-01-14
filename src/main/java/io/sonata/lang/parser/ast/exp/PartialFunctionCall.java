/*
 * Copyright (c) 2020 Kevin Mas Ruiz
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
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
                    if (currentExpression instanceof EmptyExpression) {
                        return new FunctionCall(receiver, arguments);
                    } else {
                        return new FunctionCall(receiver, append(arguments, requireNonNullElse(next, currentExpression)));
                    }
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
