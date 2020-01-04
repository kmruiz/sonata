package io.sonata.lang.parser.ast.exp;

import io.sonata.lang.parser.ast.Node;
import io.sonata.lang.source.SourcePosition;

public class Atom extends ComposedExpression implements Expression {
    public enum Type {
        NUMERIC,
        STRING,
        IDENTIFIER,
        BOOLEAN,
        NULL,
        UNKNOWN
    }

    public final SourcePosition definition;
    public final String value;
    public final Type type;

    public static Atom unknown(SourcePosition definition) {
        return new Atom(definition, "?");
    }

    public static boolean isUnknownAtom(Node node) {
        return node instanceof Atom && ((Atom) node).type == Type.UNKNOWN;
    }

    public Atom(SourcePosition definition, String value) {
        this.definition = definition;
        this.value = value;

        if (value.matches("\\d+(\\.\\d+)?")) {
            type = Type.NUMERIC;
        } else if (value.startsWith("'") && value.endsWith("'")) {
            type = Type.STRING;
        } else if (value.equals("?")) {
            type = Type.UNKNOWN;
        } else if (value.equals("true") || value.equals("false")) {
            type = Type.BOOLEAN;
        } else if (value.equals("null")) {
            type = Type.NULL;
        } else {
            type = Type.IDENTIFIER;
        }
    }

    @Override
    public String representation() {
        return value;
    }

    @Override
    public SourcePosition definition() {
        return definition;
    }
}
