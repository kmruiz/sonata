package io.sonata.lang.tokenizer.token;

import java.util.Optional;

public class StringToken implements Token {
    public static boolean isStringStart(char c) {
        return c == '\'';
    }

    public final String value;
    public final boolean escaping;
    public final boolean done;

    public StringToken(String value, boolean escaping, boolean done) {
        this.value = value;
        this.escaping = escaping;
        this.done = done;
    }

    @Override
    public String representation() {
        return value;
    }

    @Override
    public Optional<Token> nextToken(char character) {
        if (done) {
            return Optional.empty();

        }
        if (escaping) {
            return Optional.of(new StringToken(value + character, false, false));
        }

        if (character == '\\') {
            return Optional.of(new StringToken(value + character, true, false));
        }

        if (character == '\'') {
            return Optional.of(new StringToken(value + character, false, true));
        }

        return Optional.of(new StringToken(value + character, false, false));
    }
}
