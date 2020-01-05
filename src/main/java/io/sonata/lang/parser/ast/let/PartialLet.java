package io.sonata.lang.parser.ast.let;

import io.sonata.lang.exception.ParserException;
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
            } else if (token instanceof SeparatorToken) {
                SeparatorToken sep = (SeparatorToken) token;

                if (sep.separator.equals("(")) {
                    return PartialLetFunction.anonymous(definition);
                }
            }

            throw new ParserException(this, "Expected an identifier or an opening parenthesis, but got '" + token.representation() + "'");
        } else {
            if (token instanceof SeparatorToken) {
                SeparatorToken sep = (SeparatorToken) token;

                if (sep.separator.equals("(")) {
                    return PartialLetFunction.initial(definition, letName);
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
