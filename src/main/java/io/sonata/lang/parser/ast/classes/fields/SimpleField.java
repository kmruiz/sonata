package io.sonata.lang.parser.ast.classes.fields;

import io.sonata.lang.parser.ast.type.EmptyType;
import io.sonata.lang.parser.ast.type.Type;
import io.sonata.lang.source.SourcePosition;
import io.sonata.lang.tokenizer.token.IdentifierToken;
import io.sonata.lang.tokenizer.token.SeparatorToken;
import io.sonata.lang.tokenizer.token.Token;

public class SimpleField implements Field {
    enum State {
        WAITING_NAME, WAITING_SEPARATOR, WAITING_TYPE, END
    }

    private SimpleField(SourcePosition definition, String name, Type type, State state) {
        this.definition = definition;
        this.name = name;
        this.type = type;
        this.state = state;
    }

    public static SimpleField instance(SourcePosition definition) {
        return new SimpleField(definition, null, EmptyType.instance(), State.WAITING_NAME);
    }

    public final SourcePosition definition;
    public final String name;
    public final Type type;
    public final State state;

    @Override
    public String representation() {
        return name + ": " + type.representation();
    }

    @Override
    public Field consume(Token token) {
        switch (state) {
            case WAITING_NAME:
                if (token instanceof IdentifierToken) {
                    return new SimpleField(definition, token.representation(), type, State.WAITING_SEPARATOR);
                }

                return null;
            case WAITING_SEPARATOR:
                if (token instanceof SeparatorToken) {
                    SeparatorToken sep = (SeparatorToken) token;
                    if (sep.separator.equals(":")) {
                        return new SimpleField(definition, name, type, State.WAITING_TYPE);
                    }
                }

                return null;
            case WAITING_TYPE:
                Type next = type.consume(token);
                if (next == null) {
                    return new SimpleField(definition, name, type, State.END);
                }

                return new SimpleField(definition, name, next, State.WAITING_TYPE);
        }

        return null;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public boolean isDone() {
        return state == State.END;
    }

    @Override
    public SourcePosition definition() {
        return definition;
    }
}
