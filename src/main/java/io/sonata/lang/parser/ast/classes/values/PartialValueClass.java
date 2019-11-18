package io.sonata.lang.parser.ast.classes.values;

import io.sonata.lang.parser.ast.Node;
import io.sonata.lang.parser.ast.classes.fields.Field;
import io.sonata.lang.parser.ast.classes.fields.SimpleField;
import io.sonata.lang.tokenizer.token.Token;

import java.util.List;
import java.util.stream.Collectors;

import static io.sonata.lang.javaext.Lists.append;
import static java.util.Collections.emptyList;

public class PartialValueClass implements Node {
    enum State {
        WAITING_FOR_CLASS_KW, WAITING_FOR_CLASS_NAME, WAITING_FIELDS, IN_FIELDS
    }

    private final String name;
    private final List<Field> definedFields;
    private final Field currentField;
    private final State state;

    private PartialValueClass(String name, List<Field> definedFields, Field currentField, State state) {
        this.name = name;
        this.definedFields = definedFields;
        this.currentField = currentField;
        this.state = state;
    }

    public static PartialValueClass initial() {
        return new PartialValueClass(null, emptyList(), null, State.WAITING_FOR_CLASS_KW);
    }

    @Override
    public Node consume(Token token) {
        switch (state) {
            case WAITING_FOR_CLASS_KW:
                if (token.representation().equals("class")) {
                    return new PartialValueClass(null, emptyList(), SimpleField.instance(), State.WAITING_FOR_CLASS_NAME);
                }

                return null;
            case WAITING_FOR_CLASS_NAME:
                return new PartialValueClass(token.representation(), emptyList(), SimpleField.instance(), State.WAITING_FIELDS);
            case WAITING_FIELDS:
                return new PartialValueClass(name, emptyList(), SimpleField.instance(), State.IN_FIELDS);
            case IN_FIELDS:
                Field nextField = currentField.consume(token);
                if (nextField == null) {
                    return finalization(token, currentField);
                }

                if (nextField.isDone()) {
                    return finalization(token, nextField);
                }

                return new PartialValueClass(name, definedFields, nextField, state);
        }

        return null;
    }

    private Node finalization(Token token, Field currentField) {
        switch (token.representation()) {
            case ",":
                return new PartialValueClass(name, append(definedFields, currentField), SimpleField.instance(), state);
            case ")":
                return new ValueClass(name, append(definedFields, currentField));
        }
        return null;
    }

    @Override
    public String representation() {
        return String.format("value class %s(%s, %s?) %s?",
                name,
                definedFields.stream().map(Node::representation).collect(Collectors.joining(",")),
                currentField.representation(),
                state);
    }
}
