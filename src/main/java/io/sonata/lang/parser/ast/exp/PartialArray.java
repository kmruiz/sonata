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
