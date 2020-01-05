/*
 * Copyright (c) 2020 Kevin Mas Ruiz
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package io.sonata.lang.analyzer.typeSystem;

import io.sonata.lang.analyzer.Processor;
import io.sonata.lang.exception.SonataSyntaxError;
import io.sonata.lang.log.CompilerLog;
import io.sonata.lang.parser.ast.Node;
import io.sonata.lang.parser.ast.ScriptNode;
import io.sonata.lang.parser.ast.classes.fields.Field;
import io.sonata.lang.parser.ast.classes.fields.SimpleField;
import io.sonata.lang.parser.ast.classes.values.ValueClass;
import io.sonata.lang.parser.ast.type.BasicASTType;
import io.sonata.lang.parser.ast.type.ASTType;

import java.util.Optional;

public final class ClassRelationshipValidator implements Processor {
    private final Scope scope;
    private final CompilerLog log;

    public ClassRelationshipValidator(CompilerLog log, Scope scope) {
        this.scope = scope;
        this.log = log;
    }

    @Override
    public String phase() {
        return "CLASS RELATIONSHIP VALIDATION";
    }

    @Override
    public Node apply(Node node) {
        if (node instanceof ScriptNode) {
            ((ScriptNode) node).nodes.forEach(this::apply);
        }

        if (node instanceof ValueClass) {
            ValueClass vc = (ValueClass) node;
            vc.definedFields.forEach(field -> this.validateIsNotAnEntity(vc, field));
        }

        return node;
    }

    private void validateIsNotAnEntity(ValueClass valueClass, Field field) {
        ASTType fieldASTType = ((SimpleField) field).astType;

        if (fieldASTType instanceof BasicASTType) {
            String fieldName = ((SimpleField) field).name;
            String typeName = ((BasicASTType) fieldASTType).name;
            final Optional<Type> resolution = scope.resolveType(typeName);
            if (!resolution.isPresent()) {
                log.syntaxError(new SonataSyntaxError(fieldASTType, "Couldn't resolve type '" + typeName + "'"));
            } else {
                Type type = resolution.get();
                if (type.isEntity()) {
                    log.syntaxError(new SonataSyntaxError(field, "Value classes can not depend on entities, but value class '" + valueClass.name + "' has a field named '" + fieldName + "' of type '" + type.name() + "' defined on " + type.definition() + " as an entity."));
                }
            }
        }
    }
}
