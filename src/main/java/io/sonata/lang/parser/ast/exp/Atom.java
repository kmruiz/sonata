package io.sonata.lang.parser.ast.exp;

public class Atom extends ComposedExpression implements Expression {
    public final String value;

    public Atom(String value) {
        this.value = value;
    }

    @Override
    public String representation() {
        return value;
    }
}
