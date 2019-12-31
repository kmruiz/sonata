package io.sonata.lang.parser.ast.classes.entities;

import io.sonata.lang.parser.ast.Node;
import io.sonata.lang.parser.ast.classes.fields.Field;
import io.sonata.lang.tokenizer.token.Token;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class EntityClass implements Node {
    public final String name;
    public final List<Field> definedFields;
    public final List<Node> body;

    public EntityClass(String name, List<Field> definedFields) {
        this(name, definedFields, Collections.emptyList());
    }

    public EntityClass(String name, List<Field> definedFields, List<Node> body) {
        this.name = name;
        this.definedFields = definedFields;
        this.body = body;
    }

    @Override
    public Node consume(Token token) {
        if (token.representation().equals("{")) {
            return PartialEntityClassWithBody.initial(name, definedFields);
        }

        return null;
    }

    @Override
    public String representation() {
        return String.format("entity class %s(%s) {\n\t%s\n}",
                name,
                definedFields.stream().map(Node::representation).collect(Collectors.joining(", ")),
                body.stream().map(Node::representation).collect(Collectors.joining("\n\t"))
        );
    }
}
