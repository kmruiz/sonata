package io.sonata.lang.tokenizer.token;

import io.sonata.lang.source.SourceCharacter;
import io.sonata.lang.source.SourcePosition;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class SeparatorToken implements Token {
    private static final Set<Character> SEPARATORS;

    static {
        SEPARATORS = new HashSet<>();
        SEPARATORS.add('(');
        SEPARATORS.add(')');
        SEPARATORS.add('[');
        SEPARATORS.add(']');
        SEPARATORS.add('{');
        SEPARATORS.add('}');
        SEPARATORS.add(':');
        SEPARATORS.add(',');
        SEPARATORS.add(';');
        SEPARATORS.add('.');
        SEPARATORS.add('?');
        SEPARATORS.add('\n');
        SEPARATORS.add('\0');
    }

    public static boolean isSeparator(char c) {
        return SEPARATORS.contains(c);
    }

    public final String separator;
    public final SourcePosition sourcePosition;

    public SeparatorToken(SourcePosition sourcePosition, String separator) {
        this.sourcePosition = sourcePosition;
        this.separator = separator;
    }

    @Override
    public String representation() {
        return separator;
    }

    @Override
    public Optional<Token> nextToken(SourceCharacter character) {
        return Optional.empty();
    }

    @Override
    public SourcePosition sourcePosition() {
        return sourcePosition;
    }
}
