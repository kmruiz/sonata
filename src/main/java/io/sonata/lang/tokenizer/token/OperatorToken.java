package io.sonata.lang.tokenizer.token;

import java.util.Optional;
import java.util.Set;

public class OperatorToken implements Token {
    private static final Set<Character> OPERATORS = Set.of(
            '<', '>', '!', '#', '$', '%', '&', '^', '/', '=', '+', '-', '*', '~', '|'
    );

    public static boolean isOperator(char c) {
        return OPERATORS.contains(c);
    }

    public final String operator;

    public OperatorToken(String operator) {
        this.operator = operator;
    }

    @Override
    public String representation() {
        return operator;
    }

    @Override
    public Optional<Token> nextToken(char character) {
        if (isOperator(character)) {
            return Optional.of(new OperatorToken(operator + character));
        }

        return Optional.empty();
    }
}
