package io.sonata.lang.parser.ast.classes.entities;

import io.sonata.lang.parser.ast.Node;
import io.sonata.lang.parser.ast.RootNode;
import io.sonata.lang.parser.ast.classes.fields.Field;
import io.sonata.lang.source.SourcePosition;
import io.sonata.lang.tokenizer.token.Token;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static io.sonata.lang.javaext.Lists.append;

public class PartialEntityClassWithBody implements Node {
    private final SourcePosition definition;
    private final String name;
    private final List<Field> definedFields;
    private final List<Node> declarations;
    private final Node current;

    private PartialEntityClassWithBody(SourcePosition definition, String name, List<Field> definedFields, List<Node> declarations, Node current) {
        this.definition = definition;
        this.name = name;
        this.definedFields = definedFields;
        this.declarations = declarations;
        this.current = current;
    }

    public static PartialEntityClassWithBody initial(SourcePosition definition, String name, List<Field> definedFields) {
        return new PartialEntityClassWithBody(definition, name, definedFields, Collections.emptyList(), RootNode.instance());
    }

    @Override
    public String representation() {
        return String.format("value class %s(%s) {\n\t%s\n}",
                name,
                definedFields.stream().map(Node::representation).collect(Collectors.joining(", ")),
                declarations.stream().map(Node::representation).collect(Collectors.joining("\n\t"))
        );
    }

    @Override
    public Node consume(Token token) {
        Node nextExpr = current.consume(token);
        if (nextExpr == null) {
            if (token.representation().equals("}")) {
                return new EntityClass(definition, name, definedFields, append(declarations, current));
            }

            return new PartialEntityClassWithBody(definition, name, definedFields, append(declarations, current), RootNode.instance().consume(token));
        }

        return new PartialEntityClassWithBody(definition, name, definedFields, declarations, nextExpr);
    }

    @Override
    public SourcePosition definition() {
        return definition;
    }
}
