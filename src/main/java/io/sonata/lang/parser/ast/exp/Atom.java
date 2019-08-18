package io.sonata.lang.parser.ast.exp;

public class Atom extends ComposedExpression implements Expression {
    public enum Type {
        NUMERIC,
        STRING,
        IDENTIFIER,
        UNKNOWN
    }

    public final String value;
    public final Type type;

    public static Atom unknown() {
        return new Atom("?");
    }

    public Atom(String value) {
        this.value = value;

        if (value.matches("\\d+(\\.\\d+)?")) {
            type = Type.NUMERIC;
        } else if (value.startsWith("'") && value.endsWith("'")) {
            type = Type.STRING;
        } else if (value.equals("?")) {
            type = Type.UNKNOWN;
        } else {
            type = Type.IDENTIFIER;
        }
    }

    @Override
    public String representation() {
        return value;
    }
}
