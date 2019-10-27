package io.sonata.lang.tokenizer.token;

import io.sonata.lang.source.SourceCharacter;
import io.sonata.lang.source.SourcePosition;

import java.util.Optional;

public class NumericToken implements Token {
    public static boolean isNumeric(char c) {
        return Character.isDigit(c);
    }

    public final String value;
    public final SourcePosition sourcePosition;

    public NumericToken(SourcePosition sourcePosition, String value) {
        this.sourcePosition = sourcePosition;
        this.value = value;
    }

    @Override
    public String representation() {
        return value;
    }

    @Override
    public Optional<Token> nextToken(SourceCharacter character) {
        if (isNumeric(character.character) || character.character == '_') {
            return Optional.of(new NumericToken(sourcePosition, value + character.character));
        }

        if (character.character == '.' && !value.contains(".")) {
            return Optional.of(new NumericToken(sourcePosition, value + character.character));
        }

        return Optional.empty();
    }

    @Override
    public SourcePosition sourcePosition() {
        return sourcePosition;
    }
}
