/*
 * Copyright (c) 2020 Kevin Mas Ruiz
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package io.sonata.lang.parser.ast.exp.ifelse;

import io.sonata.lang.parser.ast.exp.EmptyExpression;
import io.sonata.lang.parser.ast.exp.Expression;
import io.sonata.lang.parser.ast.exp.IfElse;
import io.sonata.lang.source.SourcePosition;
import io.sonata.lang.tokenizer.token.Token;

public class PartialIfElse implements Expression {
    private PartialIfElse(SourcePosition definition, Expression condition, Expression whenTrue, Expression whenFalse) {
        this.definition = definition;
        this.condition = condition;
        this.whenTrue = whenTrue;
        this.whenFalse = whenFalse;
    }

    public static Expression from(SourcePosition sourcePosition, Expression condition, Expression whenTrue) {
        return new PartialIfElse(sourcePosition, condition, whenTrue, EmptyExpression.instance());
    }

    private final SourcePosition definition;
    private final Expression condition;
    private final Expression whenTrue;
    private final Expression whenFalse;

    @Override
    public Expression consume(Token token) {
        Expression nextBody = whenFalse.consume(token);
        if (nextBody == null) {
            return new IfElse(definition, condition, whenTrue, whenFalse);
        }

        return new PartialIfElse(definition, condition, whenTrue, nextBody);
    }

    @Override
    public SourcePosition definition() {
        return definition;
    }

    @Override
    public String representation() {
        return "if (" + condition.representation() + ") " + whenTrue.representation();
    }
}
