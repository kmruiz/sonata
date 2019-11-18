package io.sonata.lang.tokenizer.token;

import io.sonata.lang.source.SourceCharacter;
import io.sonata.lang.source.SourcePosition;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class OperatorToken implements Token {
    private static final Set<Character> OPERATORS;

    static {
        OPERATORS = new HashSet<>();
        OPERATORS.add('<');
        OPERATORS.add('>');
        OPERATORS.add('!');
        OPERATORS.add('#');
        OPERATORS.add('$');
        OPERATORS.add('%');
        OPERATORS.add('&');
        OPERATORS.add('^');
        OPERATORS.add('/');
        OPERATORS.add('=');
        OPERATORS.add('+');
        OPERATORS.add('-');
        OPERATORS.add('*');
        OPERATORS.add('~');
        OPERATORS.add('|');
    }

    public static boolean isOperator(char c) {
        return OPERATORS.contains(c);
    }

    public final String operator;
    public final SourcePosition sourcePosition;

    public OperatorToken(SourcePosition sourcePosition, String operator) {
        this.sourcePosition = sourcePosition;
        this.operator = operator;
    }

    @Override
    public String representation() {
        return operator;
    }

    @Override
    public Optional<Token> nextToken(SourceCharacter character) {
        if (isOperator(character.character)) {
            return Optional.of(new OperatorToken(sourcePosition, operator + character.character));
        }

        return Optional.empty();
    }

    @Override
    public SourcePosition sourcePosition() {
        return sourcePosition;
    }
}
