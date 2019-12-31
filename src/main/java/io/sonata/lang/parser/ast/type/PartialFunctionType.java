package io.sonata.lang.parser.ast.type;

import io.sonata.lang.source.SourcePosition;
import io.sonata.lang.tokenizer.token.SeparatorToken;
import io.sonata.lang.tokenizer.token.Token;

import java.util.Collections;
import java.util.List;

import static io.sonata.lang.javaext.Lists.append;

public class PartialFunctionType implements Type {
    public final SourcePosition definition;
    public final Type currentParameterType;
    public final List<Type> parameters;

    private PartialFunctionType(SourcePosition definition, List<Type> parameters, Type currentParameterType, Type returnType, State state) {
        this.definition = definition;
        this.parameters = parameters;
        this.currentParameterType = currentParameterType;
        this.returnType = returnType;
        this.state = state;
    }

    public final Type returnType;
    public final State state;

    public static PartialFunctionType inParameterList(SourcePosition definition) {
        return new PartialFunctionType(definition, Collections.emptyList(), EmptyType.instance(), EmptyType.instance(), State.IN_PARAMETERS);
    }

    public static PartialFunctionType withoutParameters(SourcePosition definition) {
        return new PartialFunctionType(definition, Collections.emptyList(), EmptyType.instance(), EmptyType.instance(), State.IN_RETURN_TYPE);
    }

    @Override
    public Type consume(Token token) {
        switch (state) {
            case IN_PARAMETERS:
                Type nextParam = currentParameterType.consume(token);
                if (nextParam == null) {
                    if (token instanceof SeparatorToken) {
                        switch (token.representation()) {
                            case ",":
                                return new PartialFunctionType(definition, append(parameters, currentParameterType), EmptyType.instance(), returnType, state);
                            case ")":
                                return new PartialFunctionType(definition, append(parameters, currentParameterType), EmptyType.instance(), returnType, State.WAITING_FOR_RETURN_TYPE);
                            default:
                                return null;
                        }
                    }
                }

                return new PartialFunctionType(definition, parameters, nextParam, returnType, state);
            case WAITING_FOR_RETURN_TYPE:
                if (token.representation().equals("->")) {
                    return new PartialFunctionType(definition, parameters, currentParameterType, returnType, State.IN_RETURN_TYPE);
                }

                return null;
            case IN_RETURN_TYPE:
                Type nextRetType = returnType.consume(token);
                if (nextRetType == null) {
                    return new FunctionType(definition, parameters, returnType);
                }

                return new PartialFunctionType(definition, parameters, currentParameterType, nextRetType, State.IN_RETURN_TYPE);
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
