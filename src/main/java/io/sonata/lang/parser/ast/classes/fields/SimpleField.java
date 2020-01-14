/*
 * Copyright (c) 2020 Kevin Mas Ruiz
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package io.sonata.lang.parser.ast.classes.fields;

import io.sonata.lang.parser.ast.type.EmptyASTType;
import io.sonata.lang.parser.ast.type.ASTType;
import io.sonata.lang.source.SourcePosition;
import io.sonata.lang.tokenizer.token.IdentifierToken;
import io.sonata.lang.tokenizer.token.SeparatorToken;
import io.sonata.lang.tokenizer.token.Token;

public class SimpleField implements Field {
    enum State {
        WAITING_NAME, WAITING_SEPARATOR, WAITING_TYPE, END
    }

    private SimpleField(SourcePosition definition, String name, ASTType astType, State state) {
        this.definition = definition;
        this.name = name;
        this.astType = astType;
        this.state = state;
    }

    public static SimpleField instance(SourcePosition definition) {
        return new SimpleField(definition, null, EmptyASTType.instance(), State.WAITING_NAME);
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
    public Field consume(Token token) {
        switch (state) {
            case WAITING_NAME:
                if (token instanceof IdentifierToken) {
                    return new SimpleField(definition, token.representation(), astType, State.WAITING_SEPARATOR);
                }

                return null;
            case WAITING_SEPARATOR:
                if (token instanceof SeparatorToken) {
                    SeparatorToken sep = (SeparatorToken) token;
                    if (sep.separator.equals(":")) {
                        return new SimpleField(definition, name, astType, State.WAITING_TYPE);
                    }
                }

                return null;
            case WAITING_TYPE:
                ASTType next = astType.consume(token);
                if (next == null) {
                    return new SimpleField(definition, name, astType, State.END);
                }

                return new SimpleField(definition, name, next, State.WAITING_TYPE);
        }

        return null;
    }

    @Override
    public String name() {
        return name;
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
