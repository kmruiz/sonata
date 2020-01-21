/*
 * Copyright (c) 2020 Kevin Mas Ruiz
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package io.sonata.lang.parser.ast.exp;

import io.sonata.lang.parser.ast.type.ASTTypeRepresentation;
import io.sonata.lang.parser.ast.type.BasicASTTypeRepresentation;
import io.sonata.lang.source.SourcePosition;
import io.sonata.lang.tokenizer.token.Token;

import static io.sonata.lang.parser.ast.exp.EmptyExpression.instance;

public class SimpleExpression implements Expression {
    public final Expression leftSide;
    public final String operator;
    public final Expression rightSide;

    public SimpleExpression(Expression leftSide, String operator, Expression rightSide) {
        this.leftSide = leftSide;
        this.operator = operator;
        this.rightSide = rightSide;
    }

    public static Expression initial(Expression leftSide, String operator) {
        return new SimpleExpression(leftSide, operator, instance());
    }

    @Override
    public Expression consume(Token token) {
        Expression newRightSide = rightSide.consume(token);
        if (newRightSide == null) {
            return null;
        }

        return new SimpleExpression(leftSide, operator, newRightSide);
    }

    @Override
    public String representation() {
        return leftSide.representation() + " " + operator + " " + rightSide.representation();
    }

    @Override
    public ASTTypeRepresentation type() {
        return new BasicASTTypeRepresentation(leftSide.definition(), "any");
    }

    @Override
    public SourcePosition definition() {
        return leftSide.definition();
    }
}
