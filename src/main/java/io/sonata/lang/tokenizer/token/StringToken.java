package io.sonata.lang.tokenizer.token;

import io.sonata.lang.source.SourceCharacter;
import io.sonata.lang.source.SourcePosition;

import java.util.Optional;

public class StringToken implements Token {
    public static boolean isStringStart(char c) {
        return c == '\'';
    }

    public final String value;
    public final boolean escaping;
    public final boolean done;
    public final SourcePosition sourcePosition;

    public StringToken(SourcePosition sourcePosition, String value, boolean escaping, boolean done) {
        this.sourcePosition = sourcePosition;
        this.value = value;
        this.escaping = escaping;
        this.done = done;
    }

    @Override
    public String representation() {
        return value;
    }

    @Override
    public Optional<Token> nextToken(SourceCharacter character) {
        if (done) {
            return Optional.empty();

        }
        if (escaping) {
            return Optional.of(new StringToken(sourcePosition, value + character.character, false, false));
        }

        if (character.character == '\\') {
            return Optional.of(new StringToken(sourcePosition, value + character.character, true, false));
        }

        if (character.character == '\'') {
            return Optional.of(new StringToken(sourcePosition, value + character.character, false, true));
        }

        return Optional.of(new StringToken(sourcePosition, value + character.character, false, false));
    }

    @Override
    public SourcePosition sourcePosition() {
        return sourcePosition;
    }
}
