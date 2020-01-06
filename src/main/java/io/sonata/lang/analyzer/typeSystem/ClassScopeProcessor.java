/*
 * Copyright (c) 2020 Kevin Mas Ruiz
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package io.sonata.lang.analyzer.typeSystem;

import io.sonata.lang.analyzer.Processor;
import io.sonata.lang.analyzer.typeSystem.exception.TypeCanNotBeReassignedException;
import io.sonata.lang.exception.SonataSyntaxError;
import io.sonata.lang.log.CompilerLog;
import io.sonata.lang.parser.ast.Node;
import io.sonata.lang.parser.ast.ScriptNode;
import io.sonata.lang.parser.ast.classes.entities.EntityClass;
import io.sonata.lang.parser.ast.classes.fields.SimpleField;
import io.sonata.lang.parser.ast.classes.values.ValueClass;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class ClassScopeProcessor implements Processor {
    private final CompilerLog log;
    private final Scope rootScope;

    public ClassScopeProcessor(CompilerLog log, Scope rootScope) {
        this.log = log;
        this.rootScope = rootScope;
    }

    @Override
    public Node apply(Node node) {
        if (node instanceof ScriptNode) {
            ((ScriptNode) node).nodes.forEach(this::apply);
        }

        if (node instanceof EntityClass) {
            EntityClass entityClass = (EntityClass) node;
            final String className = entityClass.name;
            try {
                Map<String, Type> fields = new HashMap<>();

                entityClass.definedFields.stream().map(f -> (SimpleField) f).forEach(field ->
                    registerFields(entityClass, fields, field)
                );

                rootScope.registerType(className, new EntityClassType(node.definition(), className, fields, Collections.emptyMap()));
            } catch (TypeCanNotBeReassignedException e) {
                log.syntaxError(new SonataSyntaxError(node, "Entity classes can not be redefined, but '" + className + "' is defined at least twice. Found on " + e.initialAssignment()));
            }
        }

        if (node instanceof ValueClass) {
            ValueClass vc = (ValueClass) node;
            final String className = vc.name;
            try {
                Map<String, Type> fields = new HashMap<>();

                vc.definedFields.stream().map(f -> (SimpleField) f).forEach(field ->
                        registerFields(vc, fields, field)
                );

                rootScope.registerType(className, new ValueClassType(node.definition(), className, fields, Collections.emptyMap()));
            } catch (TypeCanNotBeReassignedException e) {
                log.syntaxError(new SonataSyntaxError(node, "Value classes can not be redefined, but " + className + " is defined at least twice."));
            }
        }

        return node;
    }

    private void registerFields(Node owner, Map<String, Type> fields, SimpleField field) {
        final String typeName = field.astType.representation();
        Optional<Type> refType = rootScope.resolveType(typeName);
        if (!refType.isPresent()) {
            log.syntaxError(new SonataSyntaxError(owner, "Field '" + field.name + "' refers to a type '" + typeName + "', which does not exist."));
        } else {
            fields.put(field.name, refType.orElse(willBeAny()));
        }
    }

    private Type willBeAny() {
        return rootScope.resolveType("any").get();
    }

    @Override
    public String phase() {
        return "CLASS PROCESSING";
    }
}
