package io.sonata.lang.parser.ast.let;

import io.sonata.lang.parser.ast.exp.BlockExpression;
import io.sonata.lang.parser.ast.exp.EmptyExpression;
import io.sonata.lang.parser.ast.exp.Expression;
import io.sonata.lang.parser.ast.exp.Lambda;
import io.sonata.lang.parser.ast.let.fn.Parameter;
import io.sonata.lang.parser.ast.let.fn.SimpleParameter;
import io.sonata.lang.parser.ast.type.EmptyType;
import io.sonata.lang.parser.ast.type.Type;
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

    private PartialLetFunction(String letName, State state, List<Parameter> parameters, Parameter currentParameter, Type returnType, Expression body) {
        this.letName = letName;
        this.state = state;
        this.parameters = parameters;
        this.currentParameter = currentParameter;
        this.returnType = returnType;
        this.body = body;
    }

    public final String letName;
    public final State state;
    public final List<Parameter> parameters;
    public final Parameter currentParameter;
    public final Type returnType;
    public final Expression body;

    public static PartialLetFunction anonymous() {
        return new PartialLetFunction("", State.IN_PARAMETER, emptyList(), SimpleParameter.instance(), EmptyType.instance(), EmptyExpression.instance());
    }

    public static PartialLetFunction initial(String letName) {
        return new PartialLetFunction(letName, State.IN_PARAMETER, emptyList(), SimpleParameter.instance(), EmptyType.instance(), EmptyExpression.instance());
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
                                return new PartialLetFunction(letName, State.IN_PARAMETER, append(parameters, requireNonNullElse(nextParam, currentParameter)), SimpleParameter.instance(), returnType, body);
                            case ")":
                            case "]":
                                if (nextParam == null) {
                                    return new PartialLetFunction(letName, State.WAITING_DEFINITION, parameters, SimpleParameter.instance(), returnType, body);
                                } else {
                                    return new PartialLetFunction(letName, State.WAITING_DEFINITION, append(parameters, requireNonNullElse(nextParam, currentParameter)), SimpleParameter.instance(), returnType, body);
                                }
                        }
                    }
                } else {
                    return new PartialLetFunction(letName, State.IN_PARAMETER, parameters, nextParam, returnType, body);
                }
            case WAITING_DEFINITION:
                if (token instanceof OperatorToken) {
                    return new PartialLetFunction(letName, State.IN_BODY, parameters, currentParameter, returnType, body);
                } else if (token instanceof SeparatorToken && token.representation().equals("\n")) {
                    return new LetFunction(letName, parameters, returnType, null);
                } else if (token instanceof SeparatorToken && token.representation().equals(":") && returnType instanceof EmptyType) {
                    return new PartialLetFunction(letName, State.IN_RETURN_TYPE, parameters, currentParameter, returnType, body);
                }
                return this;
            case IN_RETURN_TYPE:
                Type nextType = returnType.consume(token);
                if (nextType == null) {
                    return new PartialLetFunction(letName, State.IN_BODY, parameters, currentParameter, returnType, body);
                }

                return new PartialLetFunction(letName, state, parameters, currentParameter, nextType, body);
            case IN_BODY:
                Expression nextBody = body.consume(token);
                if (nextBody == null || nextBody instanceof BlockExpression) {
                    if (letName.equals("")) {
                        return Lambda.synthetic((List) parameters, body);
                    } else {
                        return new LetFunction(letName, parameters, returnType, requireNonNullElse(nextBody, body));
                    }
                }

                return new PartialLetFunction(letName, state, parameters, currentParameter, returnType, nextBody);
        }

        return null;
    }
}
