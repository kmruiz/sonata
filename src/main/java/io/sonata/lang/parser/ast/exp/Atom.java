/*
 * Copyright (c) 2020 Kevin Mas Ruiz
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package io.sonata.lang.parser.ast.exp;

import io.sonata.lang.parser.ast.Node;
import io.sonata.lang.parser.ast.type.ASTTypeRepresentation;
import io.sonata.lang.parser.ast.type.BasicASTTypeRepresentation;
import io.sonata.lang.source.SourcePosition;

import java.util.Objects;

public class Atom extends ComposedExpression implements Expression {
    public enum Kind {
        NUMERIC,
        STRING,
        IDENTIFIER,
        BOOLEAN,
        UNKNOWN
    }

    public final SourcePosition definition;
    public final String value;
    public final Kind kind;

    public static Atom unknown(SourcePosition definition) {
        return new Atom(definition, "?");
    }

    public static boolean isUnknownAtom(Node node) {
        return node instanceof Atom && ((Atom) node).kind == Kind.UNKNOWN;
    }

    public Atom(SourcePosition definition, String value) {
        this.definition = definition;
        this.value = value;

        if (value.matches("\\d+(\\.\\d+)?")) {
            kind = Kind.NUMERIC;
        } else if (value.startsWith("'") && value.endsWith("'")) {
            kind = Kind.STRING;
        } else if (value.equals("?")) {
            kind = Kind.UNKNOWN;
        } else if (value.equals("true") || value.equals("false")) {
            kind = Kind.BOOLEAN;
        } else {
            kind = Kind.IDENTIFIER;
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
    public ASTTypeRepresentation type() {
        switch (kind) {
            case NUMERIC:
                return new BasicASTTypeRepresentation(definition, "number");
            case STRING:
                return new BasicASTTypeRepresentation(definition,"string");
            case BOOLEAN:
                return new BasicASTTypeRepresentation(definition, "boolean");
            default:
                return new BasicASTTypeRepresentation(definition, "any");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Atom atom = (Atom) o;
        return value.equals(atom.value) && kind == atom.kind;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, kind);
    }
}
