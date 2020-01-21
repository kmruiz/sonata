/*
 * Copyright (c) 2020 Kevin Mas Ruiz
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package io.sonata.lang.parser.ast.type;

import io.sonata.lang.source.SourcePosition;
import io.sonata.lang.tokenizer.token.SeparatorToken;
import io.sonata.lang.tokenizer.token.Token;

import java.util.Collections;
import java.util.List;

import static io.sonata.lang.javaext.Lists.append;

public class PartialFunctionASTTypeRepresentation implements ASTTypeRepresentation {
    public final SourcePosition definition;
    public final ASTTypeRepresentation currentParameterASTTypeRepresentation;
    public final List<ASTTypeRepresentation> parameters;

    private PartialFunctionASTTypeRepresentation(SourcePosition definition, List<ASTTypeRepresentation> parameters, ASTTypeRepresentation currentParameterASTTypeRepresentation, ASTTypeRepresentation returnASTTypeRepresentation, State state) {
        this.definition = definition;
        this.parameters = parameters;
        this.currentParameterASTTypeRepresentation = currentParameterASTTypeRepresentation;
        this.returnASTTypeRepresentation = returnASTTypeRepresentation;
        this.state = state;
    }

    public final ASTTypeRepresentation returnASTTypeRepresentation;
    public final State state;

    public static PartialFunctionASTTypeRepresentation inParameterList(SourcePosition definition) {
        return new PartialFunctionASTTypeRepresentation(definition, Collections.emptyList(), EmptyASTTypeRepresentation.instance(), EmptyASTTypeRepresentation.instance(), State.IN_PARAMETERS);
    }

    public static PartialFunctionASTTypeRepresentation withoutParameters(SourcePosition definition) {
        return new PartialFunctionASTTypeRepresentation(definition, Collections.emptyList(), EmptyASTTypeRepresentation.instance(), EmptyASTTypeRepresentation.instance(), State.IN_RETURN_TYPE);
    }

    @Override
    public ASTTypeRepresentation consume(Token token) {
        switch (state) {
            case IN_PARAMETERS:
                ASTTypeRepresentation nextParam = currentParameterASTTypeRepresentation.consume(token);
                if (nextParam == null) {
                    if (token instanceof SeparatorToken) {
                        switch (token.representation()) {
                            case ",":
                                return new PartialFunctionASTTypeRepresentation(definition, append(parameters, currentParameterASTTypeRepresentation), EmptyASTTypeRepresentation.instance(), returnASTTypeRepresentation, state);
                            case ")":
                                return new PartialFunctionASTTypeRepresentation(definition, append(parameters, currentParameterASTTypeRepresentation), EmptyASTTypeRepresentation.instance(), returnASTTypeRepresentation, State.WAITING_FOR_RETURN_TYPE);
                            default:
                                return null;
                        }
                    }
                }

                return new PartialFunctionASTTypeRepresentation(definition, parameters, nextParam, returnASTTypeRepresentation, state);
            case WAITING_FOR_RETURN_TYPE:
                if (token.representation().equals("->")) {
                    return new PartialFunctionASTTypeRepresentation(definition, parameters, currentParameterASTTypeRepresentation, returnASTTypeRepresentation, State.IN_RETURN_TYPE);
                }

                return null;
            case IN_RETURN_TYPE:
                ASTTypeRepresentation nextRetASTTypeRepresentation = returnASTTypeRepresentation.consume(token);
                if (nextRetASTTypeRepresentation == null) {
                    return new FunctionASTTypeRepresentation(definition, parameters, returnASTTypeRepresentation);
                }

                return new PartialFunctionASTTypeRepresentation(definition, parameters, currentParameterASTTypeRepresentation, nextRetASTTypeRepresentation, State.IN_RETURN_TYPE);
        }

        return null;
    }

    private enum State {
        IN_PARAMETERS,
        WAITING_FOR_RETURN_TYPE,
        IN_RETURN_TYPE,
    }

    @Override
    public String representation() {
        return "let()";
    }

    @Override
    public SourcePosition definition() {
        return definition;
    }
}
