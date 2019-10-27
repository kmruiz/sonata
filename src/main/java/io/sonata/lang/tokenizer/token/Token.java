package io.sonata.lang.tokenizer.token;

import io.sonata.lang.source.SourceCharacter;
import io.sonata.lang.source.SourcePosition;

import java.util.Optional;

public interface Token {
    SourcePosition sourcePosition();
    String representation();

    Optional<Token> nextToken(SourceCharacter character);
}
