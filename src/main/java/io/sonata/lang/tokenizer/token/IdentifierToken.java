package io.sonata.lang.tokenizer.token;

import java.util.Optional;

public class IdentifierToken implements Token {
    public static boolean isIdentifier(char character) {
        return Character.isLetter(character) || character == '_';
    }

    public final String value;

    public IdentifierToken(String value) {
        this.value = value;
    }

    @Override
    public String representation() {
        return value;
    }

    @Override
    public Optional<Token> nextToken(char character) {
        if (isIdentifier(character) || Character.isDigit(character)) {
            return Optional.of(new IdentifierToken(value + character));
        }

        return Optional.empty();
    }
}
