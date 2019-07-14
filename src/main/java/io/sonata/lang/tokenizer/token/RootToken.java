package io.sonata.lang.tokenizer.token;

import java.util.Optional;

public class RootToken implements Token {
    private static final RootToken INSTANCE = new RootToken();

    public static RootToken instance() {
        return INSTANCE;
    }

    @Override
    public String representation() {
        return "<root>";
    }

    @Override
    public Optional<Token> nextToken(char character) {
        if (IdentifierToken.isIdentifier(character)) {
            return Optional.of(new IdentifierToken(String.valueOf(character)));
        }

        if (SeparatorToken.isSeparator(character)) {
            return Optional.of(new SeparatorToken(String.valueOf(character)));
        }

        if (OperatorToken.isOperator(character)) {
            return Optional.of(new OperatorToken(String.valueOf(character)));
        }

        if (NumericToken.isNumeric(character)) {
            return Optional.of(new NumericToken(String.valueOf(character)));
        }

        if (StringToken.isStringStart(character)) {
            return Optional.of(new StringToken(String.valueOf(character), false, false));
        }

        return Optional.of(this);
    }

}
