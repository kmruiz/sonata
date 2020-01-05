package io.sonata.lang.parser.ast.let;

import io.sonata.lang.exception.ParserException;
import io.sonata.lang.parser.ast.exp.BlockExpression;
import io.sonata.lang.parser.ast.exp.EmptyExpression;
import io.sonata.lang.parser.ast.exp.Expression;
import io.sonata.lang.parser.ast.exp.Lambda;
import io.sonata.lang.parser.ast.let.fn.Parameter;
import io.sonata.lang.parser.ast.let.fn.SimpleParameter;
import io.sonata.lang.parser.ast.type.EmptyASTType;
import io.sonata.lang.parser.ast.type.ASTType;
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

    private PartialLetFunction(SourcePosition definition, String letName, State state, List<Parameter> parameters, Parameter currentParameter, ASTType returnASTType, Expression body) {
        this.definition = definition;
        this.letName = letName;
        this.state = state;
        this.parameters = parameters;
        this.currentParameter = currentParameter;
        this.returnASTType = returnASTType;
        this.body = body;
    }

    public final SourcePosition definition;
    public final String letName;
    public final State state;
    public final List<Parameter> parameters;
    public final Parameter currentParameter;
    public final ASTType returnASTType;
    public final Expression body;

    public static PartialLetFunction anonymous(SourcePosition definition) {
        return initial(definition, "");
    }

    public static PartialLetFunction initial(SourcePosition definition, String letName) {
        return new PartialLetFunction(definition, letName, State.IN_PARAMETER, emptyList(), SimpleParameter.instance(definition), EmptyASTType.instance(), EmptyExpression.instance());
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
                                return new PartialLetFunction(definition, letName, State.IN_PARAMETER, append(parameters, requireNonNullElse(nextParam, currentParameter)), SimpleParameter.instance(token.sourcePosition()), returnASTType, body);
                            case ")":
                            case "]":
                                if (nextParam == null) {
                                    return new PartialLetFunction(definition, letName, State.WAITING_DEFINITION, parameters, SimpleParameter.instance(token.sourcePosition()), returnASTType, body);
                                } else {
                                    return new PartialLetFunction(definition, letName, State.WAITING_DEFINITION, append(parameters, requireNonNullElse(nextParam, currentParameter)), SimpleParameter.instance(token.sourcePosition()), returnASTType, body);
                                }
                            default:
                                throw new ParserException(this, "Expecting a comma ',', a closing parenthesis ')' or a closing bracket ']', but got '" + token.representation() + "'");
                        }
                    }
                }
                return new PartialLetFunction(definition, letName, State.IN_PARAMETER, parameters, nextParam, returnASTType, body);
            case WAITING_DEFINITION:
                if (token instanceof OperatorToken && token.representation().equals("=")) {
                    return new PartialLetFunction(definition, letName, State.IN_BODY, parameters, currentParameter, returnASTType, body);
                } else if (token instanceof SeparatorToken && token.representation().equals("\n")) {
                    return new LetFunction(definition, letName, parameters, returnASTType, null);
                } else if (token instanceof SeparatorToken && token.representation().equals(":") && returnASTType instanceof EmptyASTType) {
                    return new PartialLetFunction(definition, letName, State.IN_RETURN_TYPE, parameters, currentParameter, returnASTType, body);
                }
                throw new ParserException(this, "Expecting an equals '=', a new line or a colon ':', but got '" + token.representation() + "'");
            case IN_RETURN_TYPE:
                ASTType nextASTType = returnASTType.consume(token);
                if (nextASTType == null) {
                    return new PartialLetFunction(definition, letName, State.IN_BODY, parameters, currentParameter, returnASTType, body);
                }

                return new PartialLetFunction(definition, letName, state, parameters, currentParameter, nextASTType, body);
            case IN_BODY:
                Expression nextBody = body.consume(token);
                if (nextBody == null || nextBody instanceof BlockExpression) {
                    if (letName.equals("")) {
                        return Lambda.synthetic(definition, (List) parameters, requireNonNullElse(nextBody, body));
                    } else {
                        return new LetFunction(definition, letName, parameters, returnASTType, requireNonNullElse(nextBody, body));
                    }
                }

                return new PartialLetFunction(definition, letName, state, parameters, currentParameter, returnASTType, nextBody);
        }

        throw new ParserException(this, "Parser got to an unknown state.");
    }

    @Override
    public SourcePosition definition() {
        return definition;
    }
}
