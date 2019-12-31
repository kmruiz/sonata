package io.sonata.lang.parser.ast.let.fn;

import io.sonata.lang.parser.ast.exp.Expression;
import io.sonata.lang.parser.ast.exp.LiteralArray;
import io.sonata.lang.source.SourcePosition;
import io.sonata.lang.tokenizer.token.Token;

public class ExpressionParameter implements Parameter {
    public final Expression expression;
    public final boolean done;

    private ExpressionParameter(Expression expression, boolean done) {
        this.expression = expression;
        this.done = done;
    }

    public static ExpressionParameter of(Expression exp) {
        return new ExpressionParameter(exp, false);
    }

    @Override
    public Parameter consume(Token token) {
        Expression next = expression.consume(token);
        if (next == null) {
            return new ExpressionParameter(expression, true);
        }

        if (next instanceof LiteralArray) {
            return new ExpressionParameter(next, true);
        }

        return new ExpressionParameter(next, false);
    }

    @Override
    public String representation() {
        return expression.representation();
    }

    @Override
    public boolean isDone() {
        return done;
    }

    @Override
    public SourcePosition definition() {
        return expression.definition();
    }
}
