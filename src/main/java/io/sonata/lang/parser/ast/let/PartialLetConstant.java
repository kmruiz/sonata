package io.sonata.lang.parser.ast.let;

import io.sonata.lang.exception.ParserException;
import io.sonata.lang.parser.ast.Node;
import io.sonata.lang.parser.ast.RootNode;
import io.sonata.lang.parser.ast.exp.Expression;
import io.sonata.lang.parser.ast.type.EmptyASTType;
import io.sonata.lang.parser.ast.type.FunctionASTType;
import io.sonata.lang.parser.ast.type.ASTType;
import io.sonata.lang.source.SourcePosition;
import io.sonata.lang.tokenizer.token.Token;

public class PartialLetConstant implements Expression {
    private final SourcePosition definition;
    private final String letName;
    private final ASTType astType;
    private final State state;
    private final Node value;

    private enum State {
        WAITING_TYPE, IN_TYPE, WAITING_EQUALS, IN_BODY
    }

    public static Expression initial(SourcePosition definition, String letName) {
        return new PartialLetConstant(definition, letName, EmptyASTType.instance(), State.WAITING_TYPE, RootNode.instance());
    }

    private PartialLetConstant(SourcePosition definition, String letName, ASTType astType, State state, Node value) {
        this.definition = definition;
        this.letName = letName;
        this.astType = astType;
        this.state = state;
        this.value = value;
    }

    @Override
    public String representation() {
        return "let " + letName + ": " + astType.representation() + " = " + value.representation() + "?" + state;
    }

    @Override
    public Expression consume(Token token) {
        switch (state) {
            case WAITING_TYPE:
                if (token.representation().equals(":")) {
                    return new PartialLetConstant(definition, letName, astType, State.IN_TYPE, value);
                } else if (token.representation().equals("=")) {
                    return new PartialLetConstant(definition, letName, null, State.IN_BODY, value);
                }
                throw new ParserException(this, "Expecting a colon ':' or an equals '=', but got '" + token.representation() + "'");
            case IN_TYPE:
                ASTType nextASTType = astType.consume(token);
                if (nextASTType == null) {
                    return new PartialLetConstant(definition, letName, astType, State.WAITING_EQUALS, value).consume(token);
                }

                if (nextASTType instanceof FunctionASTType) {
                    return new PartialLetConstant(definition, letName, nextASTType, State.IN_BODY, value);
                }

                return new PartialLetConstant(definition, letName, nextASTType, state, value);
            case WAITING_EQUALS:
                if (token.representation().equals("=")) {
                    return new PartialLetConstant(definition, letName, astType, State.IN_BODY, value);
                }
                throw new ParserException(this, "Expecting an equals '=', but got '"+ token.representation() + "'");
            case IN_BODY:
                Node nextBody = value.consume(token);
                if (nextBody == null && value instanceof Expression) {
                    return new LetConstant(definition, letName, astType, (Expression) value);
                }

                return new PartialLetConstant(definition, letName, astType, state, nextBody);
        }

        throw new ParserException(this, "Parser got to an unknown state.");
    }

    @Override
    public SourcePosition definition() {
        return definition;
    }
}
