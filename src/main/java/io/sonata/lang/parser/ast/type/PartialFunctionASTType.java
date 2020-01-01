package io.sonata.lang.parser.ast.type;

import io.sonata.lang.source.SourcePosition;
import io.sonata.lang.tokenizer.token.SeparatorToken;
import io.sonata.lang.tokenizer.token.Token;

import java.util.Collections;
import java.util.List;

import static io.sonata.lang.javaext.Lists.append;

public class PartialFunctionASTType implements ASTType {
    public final SourcePosition definition;
    public final ASTType currentParameterASTType;
    public final List<ASTType> parameters;

    private PartialFunctionASTType(SourcePosition definition, List<ASTType> parameters, ASTType currentParameterASTType, ASTType returnASTType, State state) {
        this.definition = definition;
        this.parameters = parameters;
        this.currentParameterASTType = currentParameterASTType;
        this.returnASTType = returnASTType;
        this.state = state;
    }

    public final ASTType returnASTType;
    public final State state;

    public static PartialFunctionASTType inParameterList(SourcePosition definition) {
        return new PartialFunctionASTType(definition, Collections.emptyList(), EmptyASTType.instance(), EmptyASTType.instance(), State.IN_PARAMETERS);
    }

    public static PartialFunctionASTType withoutParameters(SourcePosition definition) {
        return new PartialFunctionASTType(definition, Collections.emptyList(), EmptyASTType.instance(), EmptyASTType.instance(), State.IN_RETURN_TYPE);
    }

    @Override
    public ASTType consume(Token token) {
        switch (state) {
            case IN_PARAMETERS:
                ASTType nextParam = currentParameterASTType.consume(token);
                if (nextParam == null) {
                    if (token instanceof SeparatorToken) {
                        switch (token.representation()) {
                            case ",":
                                return new PartialFunctionASTType(definition, append(parameters, currentParameterASTType), EmptyASTType.instance(), returnASTType, state);
                            case ")":
                                return new PartialFunctionASTType(definition, append(parameters, currentParameterASTType), EmptyASTType.instance(), returnASTType, State.WAITING_FOR_RETURN_TYPE);
                            default:
                                return null;
                        }
                    }
                }

                return new PartialFunctionASTType(definition, parameters, nextParam, returnASTType, state);
            case WAITING_FOR_RETURN_TYPE:
                if (token.representation().equals("->")) {
                    return new PartialFunctionASTType(definition, parameters, currentParameterASTType, returnASTType, State.IN_RETURN_TYPE);
                }

                return null;
            case IN_RETURN_TYPE:
                ASTType nextRetASTType = returnASTType.consume(token);
                if (nextRetASTType == null) {
                    return new FunctionASTType(definition, parameters, returnASTType);
                }

                return new PartialFunctionASTType(definition, parameters, currentParameterASTType, nextRetASTType, State.IN_RETURN_TYPE);
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
