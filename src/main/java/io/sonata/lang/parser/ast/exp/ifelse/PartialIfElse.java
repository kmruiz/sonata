package io.sonata.lang.parser.ast.exp.ifelse;

import io.sonata.lang.parser.ast.exp.EmptyExpression;
import io.sonata.lang.parser.ast.exp.Expression;
import io.sonata.lang.parser.ast.exp.IfElse;
import io.sonata.lang.source.SourcePosition;
import io.sonata.lang.tokenizer.token.Token;

public class PartialIfElse implements Expression {
    private PartialIfElse(SourcePosition definition, Expression condition, Expression whenTrue, Expression whenFalse) {
        this.definition = definition;
        this.condition = condition;
        this.whenTrue = whenTrue;
        this.whenFalse = whenFalse;
    }

    public static Expression from(SourcePosition sourcePosition, Expression condition, Expression whenTrue) {
        return new PartialIfElse(sourcePosition, condition, whenTrue, EmptyExpression.instance());
    }

    private final SourcePosition definition;
    private final Expression condition;
    private final Expression whenTrue;
    private final Expression whenFalse;

    @Override
    public Expression consume(Token token) {
        Expression nextBody = whenFalse.consume(token);
        if (nextBody == null) {
            return new IfElse(definition, condition, whenTrue, whenFalse);
        }

        return new PartialIfElse(definition, condition, whenTrue, nextBody);
    }

    @Override
    public SourcePosition definition() {
        return definition;
    }

    @Override
    public String representation() {
        return "if (" + condition.representation() + ") " + whenTrue.representation();
    }
}
