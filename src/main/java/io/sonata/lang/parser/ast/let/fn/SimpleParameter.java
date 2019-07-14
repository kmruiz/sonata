package io.sonata.lang.parser.ast.let.fn;

import io.sonata.lang.parser.ast.exp.Atom;
import io.sonata.lang.parser.ast.type.EmptyType;
import io.sonata.lang.parser.ast.type.Type;
import io.sonata.lang.tokenizer.token.IdentifierToken;
import io.sonata.lang.tokenizer.token.SeparatorToken;
import io.sonata.lang.tokenizer.token.Token;

public class SimpleParameter implements Parameter {
    enum State {
        WAITING_NAME, WAITING_SEPARATOR, WAITING_TYPE, END
    }

    private SimpleParameter(String name, Type type, State state) {
        this.name = name;
        this.type = type;
        this.state = state;
    }

    public static SimpleParameter instance() {
        return new SimpleParameter(null, EmptyType.instance(), State.WAITING_NAME);
    }

    public final String name;
    public final Type type;
    public final State state;

    @Override
    public String representation() {
        return name + ": " + type.representation();
    }

    @Override
    public Parameter consume(Token token) {
        switch (state) {
            case WAITING_NAME:
                if (token instanceof IdentifierToken) {
                    return new SimpleParameter(token.representation(), type, State.WAITING_SEPARATOR);
                }

                return null;
            case WAITING_SEPARATOR:
                if (token instanceof SeparatorToken) {
                    var sep = (SeparatorToken) token;
                    if (sep.separator.equals(":")) {
                        return new SimpleParameter(name, type, State.WAITING_TYPE);
                    }
                }

                return ExpressionParameter.of(new Atom(name).consume(token));
            case WAITING_TYPE:
                var next = type.consume(token);
                if (next == null) {
                    return new SimpleParameter(name, type, State.END);
                }

                return new SimpleParameter(name, next, State.WAITING_TYPE);
        }

        return null;
    }

    @Override
    public boolean isDone() {
        return state == State.END;
    }
}
