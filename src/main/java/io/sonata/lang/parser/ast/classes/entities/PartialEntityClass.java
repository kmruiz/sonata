/*
 * Copyright (c) 2020 Kevin Mas Ruiz
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package io.sonata.lang.parser.ast.classes.entities;

import io.sonata.lang.parser.ast.Node;
import io.sonata.lang.parser.ast.classes.fields.Field;
import io.sonata.lang.parser.ast.classes.fields.SimpleField;
import io.sonata.lang.source.SourcePosition;
import io.sonata.lang.tokenizer.token.Token;

import java.util.List;
import java.util.stream.Collectors;

import static io.sonata.lang.javaext.Lists.append;
import static java.util.Collections.emptyList;

public class PartialEntityClass implements Node {
    private final SourcePosition definition;
    private final String name;
    private final List<Field> definedFields;
    private final Field currentField;
    private final State state;

    private PartialEntityClass(SourcePosition definition, String name, List<Field> definedFields, Field currentField, State state) {
        this.definition = definition;
        this.name = name;
        this.definedFields = definedFields;
        this.currentField = currentField;
        this.state = state;
    }

    public static PartialEntityClass initial(SourcePosition definition) {
        return new PartialEntityClass(definition, null, emptyList(), null, State.WAITING_FOR_CLASS_KW);
    }

    @Override
    public Node consume(Token token) {
        switch (state) {
            case WAITING_FOR_CLASS_KW:
                if (token.representation().equals("class")) {
                    return new PartialEntityClass(definition, null, emptyList(), SimpleField.instance(token.sourcePosition()), State.WAITING_FOR_CLASS_NAME);
                }

                return null;
            case WAITING_FOR_CLASS_NAME:
                return new PartialEntityClass(definition, token.representation(), emptyList(), SimpleField.instance(token.sourcePosition()), State.WAITING_FIELDS);
            case WAITING_FIELDS:
                if (token.representation().equals("{")) {
                    return PartialEntityClassWithBody.initial(definition, name, emptyList(), emptyList());
                }

                return new PartialEntityClass(definition, name, emptyList(), SimpleField.instance(token.sourcePosition()), State.IN_FIELDS);
            case IN_FIELDS:
                Field nextField = currentField.consume(token);
                if (nextField == null) {
                    return finalization(token, currentField);
                }

                if (nextField.isDone()) {
                    return finalization(token, nextField);
                }

                return new PartialEntityClass(definition, name, definedFields, nextField, state);
        }

        return null;
    }

    private Node finalization(Token token, Field currentField) {
        switch (token.representation()) {
            case ",":
                return new PartialEntityClass(definition, name, append(definedFields, currentField), SimpleField.instance(token.sourcePosition()), state);
            case ")":
                if (!currentField.isDone()) {
                    return new EntityClass(definition, name, definedFields);
                } else {
                    return new EntityClass(definition, name, append(definedFields, currentField));
                }
        }
        return null;
    }

    @Override
    public String representation() {
        return String.format("entity class %s(%s, %s?) %s?",
                name,
                definedFields.stream().map(Node::representation).collect(Collectors.joining(",")),
                currentField.representation(),
                state);
    }

    enum State {
        WAITING_FOR_CLASS_KW, WAITING_FOR_CLASS_NAME, WAITING_FIELDS, IN_FIELDS
    }

    @Override
    public SourcePosition definition() {
        return definition;
    }
}
