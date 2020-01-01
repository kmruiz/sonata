package io.sonata.lang.parser.ast.classes.fields;

import io.sonata.lang.parser.ast.type.EmptyASTType;
import io.sonata.lang.parser.ast.type.ASTType;
import io.sonata.lang.source.SourcePosition;
import io.sonata.lang.tokenizer.token.IdentifierToken;
import io.sonata.lang.tokenizer.token.SeparatorToken;
import io.sonata.lang.tokenizer.token.Token;

public class SimpleField implements Field {
    enum State {
        WAITING_NAME, WAITING_SEPARATOR, WAITING_TYPE, END
    }

    private SimpleField(SourcePosition definition, String name, ASTType astType, State state) {
        this.definition = definition;
        this.name = name;
        this.astType = astType;
        this.state = state;
    }

    public static SimpleField instance(SourcePosition definition) {
        return new SimpleField(definition, null, EmptyASTType.instance(), State.WAITING_NAME);
    }

    public final SourcePosition definition;
    public final String name;
    public final ASTType astType;
    public final State state;

    @Override
    public String representation() {
        return name + ": " + astType.representation();
    }

    @Override
    public Field consume(Token token) {
        switch (state) {
            case WAITING_NAME:
                if (token instanceof IdentifierToken) {
                    return new SimpleField(definition, token.representation(), astType, State.WAITING_SEPARATOR);
                }

                return null;
            case WAITING_SEPARATOR:
                if (token instanceof SeparatorToken) {
                    SeparatorToken sep = (SeparatorToken) token;
                    if (sep.separator.equals(":")) {
                        return new SimpleField(definition, name, astType, State.WAITING_TYPE);
                    }
                }

                return null;
            case WAITING_TYPE:
                ASTType next = astType.consume(token);
                if (next == null) {
                    return new SimpleField(definition, name, astType, State.END);
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
