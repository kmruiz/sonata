package io.sonata.lang.tokenizer.token;

import io.sonata.lang.source.Source;
import io.sonata.lang.source.SourceCharacter;
import io.sonata.lang.source.SourcePosition;

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
    public Optional<Token> nextToken(SourceCharacter character) {
        String initial = String.valueOf(character.character);

        if (IdentifierToken.isIdentifier(character.character)) {
            return Optional.of(new IdentifierToken(character.position, initial));
        }

        if (SeparatorToken.isSeparator(character.character)) {
            return Optional.of(new SeparatorToken(character.position, initial));
        }

        if (OperatorToken.isOperator(character.character)) {
            return Optional.of(new OperatorToken(character.position, initial));
        }

        if (NumericToken.isNumeric(character.character)) {
            return Optional.of(new NumericToken(character.position, initial));
        }

        if (StringToken.isStringStart(character.character)) {
            return Optional.of(new StringToken(character.position, initial, false, false));
        }

        return Optional.of(this);
    }

    @Override
    public SourcePosition sourcePosition() {
        return SourcePosition.initial(Source.fromLiteral("<RootToken>"));
    }
}
