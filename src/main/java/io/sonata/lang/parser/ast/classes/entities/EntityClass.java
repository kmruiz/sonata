package io.sonata.lang.parser.ast.classes.entities;

import io.sonata.lang.parser.ast.Node;
import io.sonata.lang.parser.ast.classes.fields.Field;
import io.sonata.lang.source.SourcePosition;
import io.sonata.lang.tokenizer.token.Token;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class EntityClass implements Node {
    public final SourcePosition definition;
    public final String name;
    public final List<Field> definedFields;
    public final List<Node> body;

    public EntityClass(SourcePosition definition, String name, List<Field> definedFields) {
        this(definition, name, definedFields, Collections.emptyList());
    }

    public EntityClass(SourcePosition definition, String name, List<Field> definedFields, List<Node> body) {
        this.definition = definition;
        this.name = name;
        this.definedFields = definedFields;
        this.body = body;
    }

    @Override
    public Node consume(Token token) {
        if (token.representation().equals("{")) {
            return PartialEntityClassWithBody.initial(definition, name, definedFields);
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

    @Override
    public SourcePosition definition() {
        return definition;
    }
}
