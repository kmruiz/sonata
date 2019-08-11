package io.sonata.lang.parser.ast.exp;

import io.sonata.lang.tokenizer.token.Token;

import static io.sonata.lang.parser.ast.exp.EmptyExpression.instance;

public class SimpleExpression implements Expression {
    public final Expression leftSide;
    public final String operator;
    public final Expression rightSide;

    public SimpleExpression(Expression leftSide, String operator, Expression rightSide) {
        this.leftSide = leftSide;
        this.operator = operator;
        this.rightSide = rightSide;
    }

    public static Expression initial(Expression leftSide, String operator) {
        return new SimpleExpression(leftSide, operator, instance());
    }

    @Override
    public Expression consume(Token token) {
        Expression newRightSide = rightSide.consume(token);
        if (newRightSide == null) {
            return null;
        }

        return new SimpleExpression(leftSide, operator, newRightSide);
    }

    @Override
    public String representation() {
        return leftSide.representation() + " " + operator + " " + rightSide.representation();
    }
}
