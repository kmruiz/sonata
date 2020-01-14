/*
 * Copyright (c) 2020 Kevin Mas Ruiz
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package io.sonata.lang.parser.ast.exp;

import io.sonata.lang.parser.ast.Node;
import io.sonata.lang.source.SourcePosition;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Atom atom = (Atom) o;
        return value.equals(atom.value) && type == atom.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, type);
    }
}
