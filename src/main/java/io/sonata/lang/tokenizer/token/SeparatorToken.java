package io.sonata.lang.tokenizer.token;

import java.util.Optional;
import java.util.Set;

public class SeparatorToken implements Token {
    private static final Set<Character> SEPARATORS = Set.of(
            '(', ')', '[', ']', '{', '}', ':', ',', ';', '.', '?', '\n', '\0'
    );

    public static boolean isSeparator(char c) {
        return SEPARATORS.contains(c);
    }

    public final String separator;

    public SeparatorToken(String separator) {
        this.separator = separator;
    }

    @Override
    public String representation() {
        return separator;
    }

    @Override
    public Optional<Token> nextToken(char character) {
        return Optional.empty();
    }
}
