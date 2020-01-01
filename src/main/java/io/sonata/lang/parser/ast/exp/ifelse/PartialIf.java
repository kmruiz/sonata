package io.sonata.lang.parser.ast.exp.ifelse;

import io.sonata.lang.parser.ast.exp.EmptyExpression;
import io.sonata.lang.parser.ast.exp.Expression;
import io.sonata.lang.parser.ast.exp.IfElse;
import io.sonata.lang.source.SourcePosition;
import io.sonata.lang.tokenizer.token.Token;

public class PartialIf implements Expression {
    enum State {
        WAITING_CONDITION, IN_CONDITION, IN_BODY
    }

    private PartialIf(SourcePosition definition, State state, Expression condition, Expression body) {
        this.definition = definition;
        this.state = state;
        this.condition = condition;
        this.body = body;
    }

    public static Expression initial(SourcePosition sourcePosition) {
        return new PartialIf(sourcePosition, State.WAITING_CONDITION, EmptyExpression.instance(), EmptyExpression.instance());
    }

    private final SourcePosition definition;
    private final State state;
    private final Expression condition;
    private final Expression body;

    @Override
    public Expression consume(Token token) {
        switch (state) {
            case WAITING_CONDITION:
                if (token.representation().equals("(")) {
                    return new PartialIf(definition, State.IN_CONDITION, condition, body);
                }

                return null;
            case IN_CONDITION:
                Expression nextCondition = condition.consume(token);
                if (nextCondition == null) {
                    return new PartialIf(definition, State.IN_BODY, condition, body);
                }

                return new PartialIf(definition, state, nextCondition, body);
            case IN_BODY:
                Expression nextBody = body.consume(token);
                if (nextBody == null) {
                    if (token.representation().equals("else")) {
                        return PartialIfElse.from(definition, condition, body);
                    }

                    return new IfElse(definition, condition, body, null);
                }

                return new PartialIf(definition, state, condition, nextBody);
        }

        return null;
    }

    @Override
    public SourcePosition definition() {
        return definition;
    }

    @Override
    public String representation() {
        return "if (" + condition.representation() + ") " + body.representation();
    }
}
