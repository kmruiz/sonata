/*
 * Copyright (c) 2020 Kevin Mas Ruiz
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package io.sonata.lang.parser.ast.exp.ifelse;

import io.sonata.lang.parser.ast.exp.EmptyExpression;
import io.sonata.lang.parser.ast.exp.Expression;
import io.sonata.lang.parser.ast.exp.IfElse;
import io.sonata.lang.parser.ast.type.ASTType;
import io.sonata.lang.source.SourcePosition;
import io.sonata.lang.tokenizer.token.Token;

public class PartialIf implements Expression {
    enum State {
        WAITING_CONDITION, IN_CONDITION, IN_BODY
    }

    private PartialIf(SourcePosition definition, State state, Expression condition, Expression body) {
        this.definition = definition;
        this.state = state;
        this.condition = condition;
        this.body = body;
    }

    public static Expression initial(SourcePosition sourcePosition) {
        return new PartialIf(sourcePosition, State.WAITING_CONDITION, EmptyExpression.instance(), EmptyExpression.instance());
    }

    private final SourcePosition definition;
    private final State state;
    private final Expression condition;
    private final Expression body;

    @Override
    public Expression consume(Token token) {
        switch (state) {
            case WAITING_CONDITION:
                if (token.representation().equals("(")) {
                    return new PartialIf(definition, State.IN_CONDITION, condition, body);
                }

                return null;
            case IN_CONDITION:
                Expression nextCondition = condition.consume(token);
                if (nextCondition == null) {
                    return new PartialIf(definition, State.IN_BODY, condition, body);
                }

                return new PartialIf(definition, state, nextCondition, body);
            case IN_BODY:
                Expression nextBody = body.consume(token);
                if (nextBody == null) {
                    if (token.representation().equals("else")) {
                        return PartialIfElse.from(definition, condition, body);
                    }

                    return new IfElse(definition, condition, body, null);
                }

                return new PartialIf(definition, state, condition, nextBody);
        }

        return null;
    }

    @Override
    public ASTType type() {
        return null;
    }

    @Override
    public SourcePosition definition() {
        return definition;
    }

    @Override
    public String representation() {
        return "if (" + condition.representation() + ") " + body.representation();
    }
}
