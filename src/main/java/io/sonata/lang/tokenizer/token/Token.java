package io.sonata.lang.tokenizer.token;

import java.util.Optional;

public interface Token {
    String representation();
    Optional<Token> nextToken(char character);
}
