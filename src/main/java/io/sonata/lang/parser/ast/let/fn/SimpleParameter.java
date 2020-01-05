/*
 * Copyright (c) 2020 Kevin Mas Ruiz
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package io.sonata.lang.parser.ast.let.fn;

import io.sonata.lang.parser.ast.exp.Atom;
import io.sonata.lang.parser.ast.exp.EmptyExpression;
import io.sonata.lang.parser.ast.type.EmptyASTType;
import io.sonata.lang.parser.ast.type.FunctionASTType;
import io.sonata.lang.parser.ast.type.ASTType;
import io.sonata.lang.source.SourcePosition;
import io.sonata.lang.tokenizer.token.IdentifierToken;
import io.sonata.lang.tokenizer.token.SeparatorToken;
import io.sonata.lang.tokenizer.token.Token;

import static io.sonata.lang.javaext.Objects.requireNonNullElse;

public class SimpleParameter implements Parameter {
    public enum State {
        WAITING_NAME, WAITING_SEPARATOR, WAITING_TYPE, END
    }

    public SimpleParameter(SourcePosition definition, String name, ASTType astType, State state) {
        this.definition = definition;
        this.name = name;
        this.astType = astType;
        this.state = state;
    }

    public static SimpleParameter instance(SourcePosition definition) {
        return new SimpleParameter(definition, null, EmptyASTType.instance(), State.WAITING_NAME);
    }

    public final SourcePosition definition;
    public final String name;
    public final ASTType astType;
    public final State state;

    @Override
    public String representation() {
        return name + ": " + astType.representation();
    }

    @Override
    public Parameter consume(Token token) {
        switch (state) {
            case WAITING_NAME:
                if (token instanceof IdentifierToken) {
                    return new SimpleParameter(definition, token.representation(), astType, State.WAITING_SEPARATOR);
                }

                if (token instanceof SeparatorToken && token.representation().equals(")")) {
                    return null;
                }

                return ExpressionParameter.of(EmptyExpression.instance().consume(token));
            case WAITING_SEPARATOR:
                if (token instanceof SeparatorToken) {
                    SeparatorToken sep = (SeparatorToken) token;
                    if (sep.separator.equals(":")) {
                        return new SimpleParameter(definition, name, astType, State.WAITING_TYPE);
                    }
                }

                return ExpressionParameter.of(new Atom(definition, name).consume(token));
            case WAITING_TYPE:
                ASTType next = astType.consume(token);
                if (next == null || next instanceof FunctionASTType) {
                    return new SimpleParameter(definition, name, requireNonNullElse(next, astType), State.END);
                }

                return new SimpleParameter(definition, name, next, State.WAITING_TYPE);
        }

        return null;
    }

    @Override
    public boolean isDone() {
        return state == State.END;
    }

    @Override
    public SourcePosition definition() {
        return definition;
    }
}
