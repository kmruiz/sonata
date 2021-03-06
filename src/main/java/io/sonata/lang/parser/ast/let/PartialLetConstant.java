/*
 * Copyright (c) 2020 Kevin Mas Ruiz
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package io.sonata.lang.parser.ast.let;

import io.sonata.lang.exception.ParserException;
import io.sonata.lang.parser.ast.Node;
import io.sonata.lang.parser.ast.RootNode;
import io.sonata.lang.parser.ast.exp.Expression;
import io.sonata.lang.parser.ast.type.EmptyASTTypeRepresentation;
import io.sonata.lang.parser.ast.type.FunctionASTTypeRepresentation;
import io.sonata.lang.parser.ast.type.ASTTypeRepresentation;
import io.sonata.lang.source.SourcePosition;
import io.sonata.lang.tokenizer.token.Token;

public class PartialLetConstant implements Expression {
    private final SourcePosition definition;
    private final String letName;
    private final ASTTypeRepresentation astTypeRepresentation;
    private final State state;
    private final Node value;

    private enum State {
        WAITING_TYPE, IN_TYPE, WAITING_EQUALS, IN_BODY
    }

    public static Expression initial(SourcePosition definition, String letName) {
        return new PartialLetConstant(definition, letName, EmptyASTTypeRepresentation.instance(), State.WAITING_TYPE, RootNode.instance());
    }

    private PartialLetConstant(SourcePosition definition, String letName, ASTTypeRepresentation astTypeRepresentation, State state, Node value) {
        this.definition = definition;
        this.letName = letName;
        this.astTypeRepresentation = astTypeRepresentation;
        this.state = state;
        this.value = value;
    }

    @Override
    public String representation() {
        return "let " + letName + ": " + astTypeRepresentation.representation() + " = " + value.representation() + "?" + state;
    }

    @Override
    public Expression consume(Token token) {
        switch (state) {
            case WAITING_TYPE:
                if (token.representation().equals(":")) {
                    return new PartialLetConstant(definition, letName, astTypeRepresentation, State.IN_TYPE, value);
                } else if (token.representation().equals("=")) {
                    return new PartialLetConstant(definition, letName, null, State.IN_BODY, value);
                }
                throw new ParserException(this, "Expecting a colon ':' or an equals '=', but got '" + token.representation() + "'");
            case IN_TYPE:
                ASTTypeRepresentation nextASTTypeRepresentation = astTypeRepresentation.consume(token);
                if (nextASTTypeRepresentation == null) {
                    return new PartialLetConstant(definition, letName, astTypeRepresentation, State.WAITING_EQUALS, value).consume(token);
                }

                if (nextASTTypeRepresentation instanceof FunctionASTTypeRepresentation) {
                    return new PartialLetConstant(definition, letName, nextASTTypeRepresentation, State.IN_BODY, value);
                }

                return new PartialLetConstant(definition, letName, nextASTTypeRepresentation, state, value);
            case WAITING_EQUALS:
                if (token.representation().equals("=")) {
                    return new PartialLetConstant(definition, letName, astTypeRepresentation, State.IN_BODY, value);
                }
                throw new ParserException(this, "Expecting an equals '=', but got '"+ token.representation() + "'");
            case IN_BODY:
                Node nextBody = value.consume(token);
                if (nextBody == null && value instanceof Expression) {
                    return new LetConstant(definition, letName, astTypeRepresentation, (Expression) value);
                }

                return new PartialLetConstant(definition, letName, astTypeRepresentation, state, nextBody);
        }

        throw new ParserException(this, "Parser got to an unknown state.");
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
