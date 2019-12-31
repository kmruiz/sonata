package io.sonata.lang.parser.ast.let;

import io.sonata.lang.parser.ast.exp.Expression;
import io.sonata.lang.source.SourcePosition;
import io.sonata.lang.tokenizer.token.IdentifierToken;
import io.sonata.lang.tokenizer.token.SeparatorToken;
import io.sonata.lang.tokenizer.token.Token;

public class PartialLet implements Expression {
    public final SourcePosition definition;
    public final String letName;

    private PartialLet(SourcePosition definition, String letName) {
        this.definition = definition;
        this.letName = letName;
    }

    public static PartialLet initial(SourcePosition definition) {
        return new PartialLet(definition, null);
    }

    @Override
    public String representation() {
        return "let " + (letName == null ? "?" : letName);
    }

    @Override
    public Expression consume(Token token) {
        if (letName == null) {
            if (token instanceof IdentifierToken) {
                return new PartialLet(definition, ((IdentifierToken) token).value);
            }

            if (token instanceof SeparatorToken) {
                SeparatorToken sep = (SeparatorToken) token;

                if (sep.separator.equals("(")) {
                    return PartialLetFunction.anonymous(token.sourcePosition());
                }
            }
        } else {
            if (token instanceof SeparatorToken) {
                SeparatorToken sep = (SeparatorToken) token;

                if (sep.separator.equals("(")) {
                    return PartialLetFunction.initial(token.sourcePosition(), letName);
                }
            }
        }

        return PartialLetConstant.initial(token.sourcePosition(), letName).consume(token);
    }

    @Override
    public SourcePosition definition() {
        return definition;
    }
}
