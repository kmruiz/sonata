/*
 * Copyright (c) 2020 Kevin Mas Ruiz
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package io.sonata.lang.parser.ast.let;

import io.sonata.lang.exception.ParserException;
import io.sonata.lang.parser.ast.exp.BlockExpression;
import io.sonata.lang.parser.ast.exp.EmptyExpression;
import io.sonata.lang.parser.ast.exp.Expression;
import io.sonata.lang.parser.ast.exp.Lambda;
import io.sonata.lang.parser.ast.let.fn.Parameter;
import io.sonata.lang.parser.ast.let.fn.SimpleParameter;
import io.sonata.lang.parser.ast.type.EmptyASTTypeRepresentation;
import io.sonata.lang.parser.ast.type.ASTTypeRepresentation;
import io.sonata.lang.source.SourcePosition;
import io.sonata.lang.tokenizer.token.OperatorToken;
import io.sonata.lang.tokenizer.token.SeparatorToken;
import io.sonata.lang.tokenizer.token.Token;

import java.util.List;

import static io.sonata.lang.javaext.Lists.append;
import static io.sonata.lang.javaext.Objects.requireNonNullElse;
import static java.util.Collections.emptyList;

public class PartialLetFunction implements Expression {
    private enum State {
        IN_PARAMETER, WAITING_DEFINITION, IN_RETURN_TYPE, IN_BODY
    }

    private PartialLetFunction(SourcePosition definition, String letName, State state, List<Parameter> parameters, Parameter currentParameter, ASTTypeRepresentation returnASTTypeRepresentation, Expression body) {
        this.definition = definition;
        this.letName = letName;
        this.state = state;
        this.parameters = parameters;
        this.currentParameter = currentParameter;
        this.returnASTTypeRepresentation = returnASTTypeRepresentation;
        this.body = body;
    }

    public final SourcePosition definition;
    public final String letName;
    public final State state;
    public final List<Parameter> parameters;
    public final Parameter currentParameter;
    public final ASTTypeRepresentation returnASTTypeRepresentation;
    public final Expression body;

    public static PartialLetFunction anonymous(SourcePosition definition) {
        return initial(definition, "");
    }

    public static PartialLetFunction initial(SourcePosition definition, String letName) {
        return new PartialLetFunction(definition, letName, State.IN_PARAMETER, emptyList(), SimpleParameter.instance(definition), EmptyASTTypeRepresentation.instance(), EmptyExpression.instance());
    }

    @Override
    public String representation() {
        return "let " + letName + " : " + state;
    }

    @Override
    public Expression consume(Token token) {
        switch (state) {
            case IN_PARAMETER:
                Parameter nextParam = currentParameter.consume(token);
                if (nextParam == null || nextParam.isDone()) {
                    if (token instanceof SeparatorToken) {
                        SeparatorToken separator = (SeparatorToken) token;
                        switch (separator.separator) {
                            case ",":
                                return new PartialLetFunction(definition, letName, State.IN_PARAMETER, append(parameters, requireNonNullElse(nextParam, currentParameter)), SimpleParameter.instance(token.sourcePosition()), returnASTTypeRepresentation, body);
                            case ")":
                                if (nextParam == null) {
                                    return new PartialLetFunction(definition, letName, State.WAITING_DEFINITION, parameters, SimpleParameter.instance(token.sourcePosition()), returnASTTypeRepresentation, body);
                                } else {
                                    return new PartialLetFunction(definition, letName, State.WAITING_DEFINITION, append(parameters, requireNonNullElse(nextParam, currentParameter)), SimpleParameter.instance(token.sourcePosition()), returnASTTypeRepresentation, body);
                                }
                            default:
                                return new PartialLetFunction(definition, letName, state, parameters, requireNonNullElse(nextParam, currentParameter), returnASTTypeRepresentation, body);
                        }
                    }
                }
                return new PartialLetFunction(definition, letName, State.IN_PARAMETER, parameters, nextParam, returnASTTypeRepresentation, body);
            case WAITING_DEFINITION:
                if (token instanceof OperatorToken && token.representation().equals("=")) {
                    return new PartialLetFunction(definition, letName, State.IN_BODY, parameters, currentParameter, returnASTTypeRepresentation, body);
                } else if (token instanceof SeparatorToken && token.representation().equals("\n")) {
                    return new LetFunction(definition, letName, parameters, returnASTTypeRepresentation, null);
                } else if (token instanceof SeparatorToken && token.representation().equals(":") && returnASTTypeRepresentation instanceof EmptyASTTypeRepresentation) {
                    return new PartialLetFunction(definition, letName, State.IN_RETURN_TYPE, parameters, currentParameter, returnASTTypeRepresentation, body);
                }
                throw new ParserException(this, "Expecting an equals '=', a new line or a colon ':', but got '" + token.representation() + "'");
            case IN_RETURN_TYPE:
                ASTTypeRepresentation nextASTTypeRepresentation = returnASTTypeRepresentation.consume(token);
                if (nextASTTypeRepresentation == null) {
                    return new PartialLetFunction(definition, letName, State.IN_BODY, parameters, currentParameter, returnASTTypeRepresentation, body);
                }

                return new PartialLetFunction(definition, letName, state, parameters, currentParameter, nextASTTypeRepresentation, body);
            case IN_BODY:
                Expression nextBody = body.consume(token);
                if (nextBody == null || nextBody instanceof BlockExpression) {
                    if (letName.equals("")) {
                        return Lambda.synthetic(definition, (List) parameters, requireNonNullElse(nextBody, body));
                    } else {
                        return new LetFunction(definition, letName, parameters, returnASTTypeRepresentation, requireNonNullElse(nextBody, body));
                    }
                }

                return new PartialLetFunction(definition, letName, state, parameters, currentParameter, returnASTTypeRepresentation, nextBody);
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
