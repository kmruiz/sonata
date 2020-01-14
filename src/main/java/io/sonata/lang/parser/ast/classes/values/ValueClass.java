/*
 * Copyright (c) 2020 Kevin Mas Ruiz
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package io.sonata.lang.parser.ast.classes.values;

import io.sonata.lang.parser.ast.Node;
import io.sonata.lang.parser.ast.Scoped;
import io.sonata.lang.parser.ast.classes.fields.Field;
import io.sonata.lang.source.SourcePosition;
import io.sonata.lang.tokenizer.token.Token;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ValueClass implements Node, Scoped {
    public final SourcePosition definition;
    public final String name;
    public final List<Field> definedFields;
    public final List<Node> body;

    public ValueClass(SourcePosition definition, String name, List<Field> definedFields) {
        this(definition, name, definedFields, Collections.emptyList());
    }

    public ValueClass(SourcePosition definition, String name, List<Field> definedFields, List<Node> body) {
        this.definition = definition;
        this.name = name;
        this.definedFields = definedFields;
        this.body = body;
    }

    @Override
    public Node consume(Token token) {
        if (token.representation().equals("{")) {
            return PartialValueClassWithBody.initial(definition, name, definedFields);
        }

        return null;
    }

    @Override
    public String representation() {
        return String.format("value class %s(%s) {\n\t%s\n}",
                name,
                definedFields.stream().map(Node::representation).collect(Collectors.joining(", ")),
                body.stream().map(Node::representation).collect(Collectors.joining("\n\t"))
        );
    }

    @Override
    public SourcePosition definition() {
        return definition;
    }

    @Override
    public String scopeId() {
        return "value class " + name;
    }
}
