package io.sonata.lang.parser.ast.exp;

import io.sonata.lang.exception.ParserException;
import io.sonata.lang.javaext.Maps;
import io.sonata.lang.parser.ast.RootNode;
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
                    if (atom.type == Atom.Type.IDENTIFIER) {
                        if (values.containsKey(atom)) {
                            throw new ParserException(this, "Literal records can not contain duplicate keys, but " + atom.value + " has been registered at least twice.");
                        }

                        return new PartialRecord(definition, values, State.WAITING_COLON, atom, EmptyExpression.instance());
                    }
                }

                throw new ParserException(maybeAtom, "Record keys can only be identifiers, but got '" + token.representation() + "'");
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
    public String representation() {
        return values.entrySet().stream().map(kv -> kv.getKey().representation() + ":" + kv.getValue().representation()).collect(joining(",", "{", "}"));
    }
}
