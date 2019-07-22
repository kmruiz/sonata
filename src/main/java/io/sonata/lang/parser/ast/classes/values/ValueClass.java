package io.sonata.lang.parser.ast.classes.values;

import io.sonata.lang.parser.ast.Node;
import io.sonata.lang.parser.ast.classes.fields.Field;
import io.sonata.lang.tokenizer.token.Token;

import java.util.List;
import java.util.stream.Collectors;

public class ValueClass implements Node {
    public final String name;
    public final List<Field> definedFields;

    public ValueClass(String name, List<Field> definedFields) {
        this.name = name;
        this.definedFields = definedFields;
    }

    @Override
    public Node consume(Token token) {
        return null;
    }

    @Override
    public String representation() {
        return String.format("value class %s(%s)",
                name,
                definedFields.stream().map(Node::representation).collect(Collectors.joining(","))
        );
    }
}
