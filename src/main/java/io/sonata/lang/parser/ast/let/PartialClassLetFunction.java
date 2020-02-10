/*
 * Copyright (c) 2020 Kevin Mas Ruiz
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package io.sonata.lang.parser.ast.let;

import io.sonata.lang.exception.ParserException;
import io.sonata.lang.parser.ast.exp.Expression;
import io.sonata.lang.parser.ast.type.ASTTypeRepresentation;
import io.sonata.lang.source.SourcePosition;
import io.sonata.lang.tokenizer.token.Token;

public class PartialClassLetFunction implements Expression {
    private enum State {
        WAITING_LET, WAITING_NAME, WAITING_PARAMS
    }

    public final SourcePosition definition;
    public final String letName;
    public final State state;

    private PartialClassLetFunction(SourcePosition definition, String letName, State state) {
        this.definition = definition;
        this.letName = letName;
        this.state = state;
    }

    public static PartialClassLetFunction initial(SourcePosition definition) {
        return new PartialClassLetFunction(definition, null, State.WAITING_LET);
    }

    @Override
    public String representation() {
        return "class let " + (letName == null ? "?" : letName);
    }

    @Override
    public Expression consume(Token token) {
        switch (state) {
            case WAITING_LET:
                if (!token.representation().equals("let")) {
                    throw new ParserException(this, "Expecting let keyword, but " + token.representation() + " found.");
                }
                return new PartialClassLetFunction(definition, null, State.WAITING_NAME);
            case WAITING_NAME:
                return new PartialClassLetFunction(definition, token.representation(), State.WAITING_PARAMS);
            case WAITING_PARAMS:
                if (token.representation().equals("(")) {
                    return PartialLetFunction.initialInClass(definition, letName);
                }
                throw new ParserException(this, "class lets can only be functions, not constants.");
        }

        throw new ParserException(this, "Unexpected token " + token.representation());
    }

    @Override
    public ASTTypeRepresentation type() {
        return null;
    }

    @Override
    public SourcePosition definition() {
        return definition;
    }
}
