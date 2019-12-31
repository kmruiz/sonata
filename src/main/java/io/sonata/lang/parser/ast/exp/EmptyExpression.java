package io.sonata.lang.parser.ast.exp;

import io.sonata.lang.parser.ast.let.PartialLet;
import io.sonata.lang.source.SourcePosition;
import io.sonata.lang.tokenizer.token.SeparatorToken;
import io.sonata.lang.tokenizer.token.Token;

public class EmptyExpression implements Expression {
    private static final EmptyExpression INSTANCE = new EmptyExpression();

    public static Expression instance() {
        return INSTANCE;
    }
    @Override
    public Expression consume(Token token) {
        if (token.representation().equals("(")) {
            return PartialPriorityExpression.instance();
        }

        if (token.representation().equals("[")) {
            return PartialArray.initial(token.sourcePosition());
        }

        if (token.representation().equals("?")) {
            return Atom.unknown(token.sourcePosition());
        }

        if (token.representation().equals("let")) {
            return PartialLet.initial(token.sourcePosition());
        }

        if (token.representation().equals("{")) {
            return PartialBlockExpression.initial(token.sourcePosition());
        }

        if (token instanceof SeparatorToken) {
            return null;
        }

        return new Atom(token.sourcePosition(), token.representation());
    }

    @Override
    public String representation() {
        return "<empty expression>";
    }

    @Override
    public SourcePosition definition() {
        return null;
    }
}
