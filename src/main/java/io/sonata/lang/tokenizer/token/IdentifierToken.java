package io.sonata.lang.tokenizer.token;

import io.sonata.lang.source.SourceCharacter;
import io.sonata.lang.source.SourcePosition;

import java.util.Optional;

public class IdentifierToken implements Token {
    public static boolean isIdentifier(char character) {
        return Character.isLetter(character) || character == '_';
    }

    public final String value;
    public final SourcePosition sourcePosition;

    public IdentifierToken(SourcePosition sourcePosition, String value) {
        this.sourcePosition = sourcePosition;
        this.value = value;
    }

    @Override
    public String representation() {
        return value;
    }

    @Override
    public Optional<Token> nextToken(SourceCharacter character) {
        if (isIdentifier(character.character) || Character.isDigit(character.character)) {
            return Optional.of(new IdentifierToken(sourcePosition, value + character.character));
        }

        return Optional.empty();
    }

    @Override
    public SourcePosition sourcePosition() {
        return sourcePosition;
    }
}
