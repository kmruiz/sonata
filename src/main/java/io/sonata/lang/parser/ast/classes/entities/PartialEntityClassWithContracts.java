/*
 * Copyright (c) 2020 Kevin Mas Ruiz
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package io.sonata.lang.parser.ast.classes.entities;

import io.sonata.lang.parser.ast.Node;
import io.sonata.lang.parser.ast.classes.fields.Field;
import io.sonata.lang.parser.ast.type.ASTTypeRepresentation;
import io.sonata.lang.parser.ast.type.EmptyASTTypeRepresentation;
import io.sonata.lang.source.SourcePosition;
import io.sonata.lang.tokenizer.token.Token;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static io.sonata.lang.javaext.Lists.append;

public class PartialEntityClassWithContracts implements Node {
    private final SourcePosition definition;
    private final String name;
    private final List<Field> definedFields;
    private final List<ASTTypeRepresentation> contracts;
    private final ASTTypeRepresentation current;

    private PartialEntityClassWithContracts(SourcePosition definition, String name, List<Field> definedFields, List<ASTTypeRepresentation> contracts, ASTTypeRepresentation current) {
        this.definition = definition;
        this.name = name;
        this.definedFields = definedFields;
        this.contracts = contracts;
        this.current = current;
    }

    public static PartialEntityClassWithContracts initial(SourcePosition definition, String name, List<Field> definedFields) {
        return new PartialEntityClassWithContracts(definition, name, definedFields, Collections.emptyList(), EmptyASTTypeRepresentation.instance());
    }

    @Override
    public String representation() {
        return String.format("entity class %s(%s) implements %s",
                name,
                definedFields.stream().map(Node::representation).collect(Collectors.joining(", ")),
                contracts.stream().map(Node::representation).collect(Collectors.joining(", "))
        );
    }

    @Override
    public Node consume(Token token) {
        ASTTypeRepresentation next = current.consume(token);
        if (next == null) {
            if (token.representation().equals(",")) {
                return new PartialEntityClassWithContracts(definition, name, definedFields, append(contracts, current), EmptyASTTypeRepresentation.instance());
            }

            return new EntityClass(definition, name, definedFields, append(contracts, current), Collections.emptyList()).consume(token);
        }

        return new PartialEntityClassWithContracts(definition, name, definedFields, contracts, next);
    }

    @Override
    public SourcePosition definition() {
        return definition;
    }
}
