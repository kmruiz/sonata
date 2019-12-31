package io.sonata.lang.parser.ast.let.fn;

import io.sonata.lang.parser.ast.exp.Atom;
import io.sonata.lang.parser.ast.exp.EmptyExpression;
import io.sonata.lang.parser.ast.type.EmptyType;
import io.sonata.lang.parser.ast.type.FunctionType;
import io.sonata.lang.parser.ast.type.Type;
import io.sonata.lang.source.SourcePosition;
import io.sonata.lang.tokenizer.token.IdentifierToken;
import io.sonata.lang.tokenizer.token.SeparatorToken;
import io.sonata.lang.tokenizer.token.Token;

import static io.sonata.lang.javaext.Objects.requireNonNullElse;

public class SimpleParameter implements Parameter {
    public enum State {
        WAITING_NAME, WAITING_SEPARATOR, WAITING_TYPE, END
    }

    public SimpleParameter(SourcePosition definition, String name, Type type, State state) {
        this.definition = definition;
        this.name = name;
        this.type = type;
        this.state = state;
    }

    public static SimpleParameter instance(SourcePosition definition) {
        return new SimpleParameter(definition, null, EmptyType.instance(), State.WAITING_NAME);
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
    public Parameter consume(Token token) {
        switch (state) {
            case WAITING_NAME:
                if (token instanceof IdentifierToken) {
                    return new SimpleParameter(definition, token.representation(), type, State.WAITING_SEPARATOR);
                }

                if (token instanceof SeparatorToken && token.representation().equals(")")) {
                    return null;
                }

                return ExpressionParameter.of(EmptyExpression.instance().consume(token));
            case WAITING_SEPARATOR:
                if (token instanceof SeparatorToken) {
                    SeparatorToken sep = (SeparatorToken) token;
                    if (sep.separator.equals(":")) {
                        return new SimpleParameter(definition, name, type, State.WAITING_TYPE);
                    }
                }

                return ExpressionParameter.of(new Atom(definition, name).consume(token));
            case WAITING_TYPE:
                Type next = type.consume(token);
                if (next == null || next instanceof FunctionType) {
                    return new SimpleParameter(definition, name, requireNonNullElse(next, type), State.END);
                }

                return new SimpleParameter(definition, name, next, State.WAITING_TYPE);
        }

        return null;
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
