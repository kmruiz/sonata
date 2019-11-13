package io.sonata.lang.parser.ast.type;

import io.sonata.lang.tokenizer.token.Token;

import java.util.Collections;
import java.util.List;

public class PartialFunctionType implements Type {
    public final List<Type> parameters;
    public final Type returnType;
    public final State state;

    private PartialFunctionType(List<Type> parameters, Type returnType, State state) {
        this.parameters = parameters;
        this.returnType = returnType;
        this.state = state;
    }

    public static PartialFunctionType withoutParameters() {
        return new PartialFunctionType(Collections.emptyList(), EmptyType.instance(), State.IN_RETURN_TYPE);
    }

    @Override
    public Type consume(Token token) {
        switch (state) {
            case IN_RETURN_TYPE:
                var next = returnType.consume(token);
                if (next == null) {
                    return new FunctionType(parameters, returnType);
                }

                return new PartialFunctionType(parameters, next, State.IN_RETURN_TYPE);
        }

        return null;
    }

    @Override
    public String representation() {
        return "let()";
    }

    enum State {IN_RETURN_TYPE}
}
