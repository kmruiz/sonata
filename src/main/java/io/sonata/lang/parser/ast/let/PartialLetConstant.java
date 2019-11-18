package io.sonata.lang.parser.ast.let;

import io.sonata.lang.parser.ast.Node;
import io.sonata.lang.parser.ast.RootNode;
import io.sonata.lang.parser.ast.exp.Expression;
import io.sonata.lang.parser.ast.type.EmptyType;
import io.sonata.lang.parser.ast.type.FunctionType;
import io.sonata.lang.parser.ast.type.Type;
import io.sonata.lang.tokenizer.token.Token;

public class PartialLetConstant implements Expression {
    private final String letName;
    private final Type type;
    private final State state;
    private final Node value;

    private enum State {
        WAITING_TYPE, IN_TYPE, WAITING_EQUALS, IN_BODY
    }

    public static Expression initial(String letName) {
        return new PartialLetConstant(letName, EmptyType.instance(), State.WAITING_TYPE, RootNode.instance());
    }

    private PartialLetConstant(String letName, Type type, State state, Node value) {
        this.letName = letName;
        this.type = type;
        this.state = state;
        this.value = value;
    }

    @Override
    public String representation() {
        return "let " + letName + ": " + type.representation() + " = " + value.representation() + "?" + state;
    }

    @Override
    public Expression consume(Token token) {
        switch (state) {
            case WAITING_TYPE:
                if (token.representation().equals(":")) {
                    return new PartialLetConstant(letName, type, State.IN_TYPE, value);
                } else if (token.representation().equals("=")) {
                    return new PartialLetConstant(letName, null, State.IN_BODY, value);
                }
                break;
            case IN_TYPE:
                var nextType = type.consume(token);
                if (nextType == null) {
                    return new PartialLetConstant(letName, type, State.WAITING_EQUALS, value).consume(token);
                }

                if (nextType instanceof FunctionType) {
                    return new PartialLetConstant(letName, nextType, State.IN_BODY, value);
                }

                return new PartialLetConstant(letName, nextType, state, value);
            case WAITING_EQUALS:
                if (token.representation().equals("=")) {
                    return new PartialLetConstant(letName, type, State.IN_BODY, value);
                }
                break;
            case IN_BODY:
                var nextBody = value.consume(token);
                if (nextBody == null) {
                    return new LetConstant(letName, type, (Expression) value);
                }

                return new PartialLetConstant(letName, type, state, nextBody);
        }

        return null;
    }
}
