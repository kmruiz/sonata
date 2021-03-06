/*
 * Copyright (c) 2020 Kevin Mas Ruiz
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package io.sonata.lang.parser.ast.exp;

import io.sonata.lang.exception.ParserException;
import io.sonata.lang.javaext.Maps;
import io.sonata.lang.parser.ast.RootNode;
import io.sonata.lang.parser.ast.type.ASTTypeRepresentation;
import io.sonata.lang.source.SourcePosition;
import io.sonata.lang.tokenizer.token.Token;

import java.util.LinkedHashMap;
import java.util.Map;

import static java.util.stream.Collectors.joining;

public class PartialRecord implements Expression {
    enum State {
        IN_KEY, WAITING_COLON, IN_VALUE, WAITING_COMMA_OR_END
    }

    private final SourcePosition definition;
    private final Map<Atom, Expression> values;
    private final State state;
    private final Expression currentAtom;
    private final Expression currentExpression;

    public PartialRecord(SourcePosition definition, Map<Atom, Expression> values, State state, Expression currentAtom, Expression currentExpression) {
        this.definition = definition;
        this.values = values;
        this.state = state;
        this.currentAtom = currentAtom;
        this.currentExpression = currentExpression;
    }

    public static PartialRecord waitingValue(SourcePosition definition, Atom key) {
        return new PartialRecord(definition, new LinkedHashMap<>(), State.IN_VALUE, key, EmptyExpression.instance());
    }

    @Override
    public Expression consume(Token token) {
        switch (state) {
            case IN_KEY:
                final Expression maybeAtom = EmptyExpression.instance().consume(token);
                if (maybeAtom instanceof Atom) {
                    Atom atom = (Atom) maybeAtom;
                    if (atom.kind == Atom.Kind.IDENTIFIER) {
                        if (values.containsKey(atom)) {
                            throw new ParserException(this, "Literal records can not contain duplicate keys, but " + atom.value + " has been registered at least twice.");
                        }

                        return new PartialRecord(definition, values, State.WAITING_COLON, atom, EmptyExpression.instance());
                    }
                }

            case WAITING_COLON:
                if (token.representation().equals(":")) {
                    return new PartialRecord(definition, values, State.IN_VALUE, currentAtom, EmptyExpression.instance());
                }

                throw new ParserException(RootNode.instance().consume(token), "Expecting colon ':', but got '" + token.representation() + "'");
            case IN_VALUE:
                final Expression nextValue = currentExpression.consume(token);
                if (nextValue == null) {
                    return new PartialRecord(definition, Maps.with(values, (Atom) currentAtom, currentExpression), State.WAITING_COMMA_OR_END, EmptyExpression.instance(), EmptyExpression.instance()).consume(token);
                }

                return new PartialRecord(definition, values, state, currentAtom, nextValue);
            case WAITING_COMMA_OR_END:
                switch (token.representation()) {
                    case ",":
                        return new PartialRecord(definition, values, State.IN_KEY, EmptyExpression.instance(), EmptyExpression.instance());
                    case "}":
                        return new Record(definition, values);
                    default:
                        throw new ParserException(RootNode.instance().consume(token), "Expecting comma ',' or closing brace '}', but got '" + token.representation() + "'");
                }
        }

        throw new ParserException(this, "Parser got to an unknown state.");
    }

    @Override
    public SourcePosition definition() {
        return definition;
    }

    @Override
    public ASTTypeRepresentation type() {
        return null;
    }

    @Override
    public String representation() {
        return values.entrySet().stream().map(kv -> kv.getKey().representation() + ":" + kv.getValue().representation()).collect(joining(",", "{", "}"));
    }
}
