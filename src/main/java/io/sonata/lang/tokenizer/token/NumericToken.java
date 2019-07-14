package io.sonata.lang.tokenizer.token;

import java.util.Optional;

public class NumericToken implements Token {
    public static boolean isNumeric(char c) {
        return Character.isDigit(c);
    }

    public NumericToken(String value) {
        this.value = value;
    }

    public final String value;

    @Override
    public String representation() {
        return value;
    }

    @Override
    public Optional<Token> nextToken(char character) {
        if (isNumeric(character) || character == '_') {
            return Optional.of(new NumericToken(value + character));
        }

        if (character == '.' && !value.contains(".")) {
            return Optional.of(new NumericToken(value + character));
        }

        return Optional.empty();
    }
}
