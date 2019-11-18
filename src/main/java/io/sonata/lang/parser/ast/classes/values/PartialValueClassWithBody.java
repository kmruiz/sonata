package io.sonata.lang.parser.ast.classes.values;

import io.sonata.lang.parser.ast.Node;
import io.sonata.lang.parser.ast.RootNode;
import io.sonata.lang.parser.ast.classes.fields.Field;
import io.sonata.lang.tokenizer.token.Token;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static io.sonata.lang.javaext.Lists.append;

public class PartialValueClassWithBody implements Node {
    private final String name;
    private final List<Field> definedFields;
    private final List<Node> declarations;
    private final Node current;

    private PartialValueClassWithBody(String name, List<Field> definedFields, List<Node> declarations, Node current) {
        this.name = name;
        this.definedFields = definedFields;
        this.declarations = declarations;
        this.current = current;
    }

    public static PartialValueClassWithBody initial(String name, List<Field> definedFields) {
        return new PartialValueClassWithBody(name, definedFields, Collections.emptyList(), RootNode.instance());
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
                return new ValueClass(name, definedFields, append(declarations, current));
            }

            return new PartialValueClassWithBody(name, definedFields, append(declarations, current), RootNode.instance().consume(token));
        }

        return new PartialValueClassWithBody(name, definedFields, declarations, nextExpr);
    }
}
