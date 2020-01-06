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
import io.sonata.lang.exception.ParserException;
import io.sonata.lang.exception.SonataSyntaxError;
import io.sonata.lang.log.CompilerLog;
import io.sonata.lang.parser.ast.Node;
import io.sonata.lang.parser.ast.Scoped;
import io.sonata.lang.parser.ast.ScriptNode;
import io.sonata.lang.parser.ast.classes.entities.EntityClass;
import io.sonata.lang.parser.ast.classes.fields.SimpleField;
import io.sonata.lang.parser.ast.classes.values.ValueClass;
import io.sonata.lang.parser.ast.exp.BlockExpression;
import io.sonata.lang.parser.ast.let.LetConstant;
import io.sonata.lang.parser.ast.let.LetFunction;
import io.sonata.lang.parser.ast.let.fn.SimpleParameter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public final class LetVariableProcessor implements Processor {
    private final CompilerLog log;
    private final Scope scope;

    public LetVariableProcessor(CompilerLog log, Scope scope) {
        this.log = log;
        this.scope = scope;
    }

    @Override
    public Node apply(Node node) {
        apply(scope, node);

        return node;
    }

    private void apply(Scope scope, Node node) {
        if (node instanceof ScriptNode) {
            ((ScriptNode) node).nodes.forEach(child -> apply(scope, child));
        }

        if (node instanceof EntityClass) {
            Map<String, FunctionType> methods = new HashMap<>();
            Map<String, Type> fields = new HashMap<>();

            Scope classScope = scope.diveIn((Scoped) node);
            EntityClass entity = (EntityClass) node;
            String className = entity.name;

            entity.definedFields.stream().map(f -> (SimpleField) f).forEach(field ->
                    registerFields(classScope, entity, fields, field)
            );

            entity.body.stream().filter(e -> e instanceof LetFunction).map(e -> (LetFunction) e ).forEach(method ->
                    registerMethods(classScope, methods, method)
            );

            final Optional<Type> incompleteType = classScope.resolveType(className);
            if (!incompleteType.isPresent()) {
                throw new ParserException(node, "Somehow we didn't manage to pre-register this entity class. Please fill a bug with a sample code.");
            }

            classScope.enrichType(className, new EntityClassType(node.definition(), className, fields, methods));
            entity.body.forEach(b -> apply(classScope, b));
        }

        if (node instanceof ValueClass) {
            Map<String, Type> fields = new HashMap<>();
            Map<String, FunctionType> methods = new HashMap<>();

            Scope classScope = scope.diveIn((Scoped) node);
            ValueClass vc = (ValueClass) node;
            String className = vc.name;

            vc.definedFields.stream().map(f -> (SimpleField) f).forEach(field ->
                    registerFields(classScope, vc, fields, field)
            );

            vc.body.stream().filter(e -> e instanceof LetFunction).map(e -> (LetFunction) e ).forEach(method ->
                    registerMethods(classScope, methods, method)
            );

            final Optional<Type> incompleteType = classScope.resolveType(className);
            if (!incompleteType.isPresent()) {
                throw new ParserException(node, "Somehow we didn't manage to pre-register this value class. Please fill a bug with a sample code.");
            }

            classScope.enrichType(className, new ValueClassType(node.definition(), className, fields, methods));

            vc.body.forEach(b -> apply(classScope, b));
        }

        if (node instanceof BlockExpression) {
            Scope blockScope = scope.diveIn((Scoped) node);
            ((BlockExpression) node).expressions.forEach(expr -> apply(blockScope, expr));
        }

        if (node instanceof LetConstant) {
            String letName = ((LetConstant) node).letName;
            try {
                scope.registerVariable(letName, node, scope.resolveType(((LetConstant) node).returnType.representation()).orElse(scope.resolveType("any").get()));
                apply(scope, ((LetConstant) node).body);
            } catch (TypeCanNotBeReassignedException e) {
                log.syntaxError(new SonataSyntaxError(node, "Variable '" + letName + "' has been already defined. Found on " + e.initialAssignment()));
            }
        }

        if (node instanceof LetFunction) {
            String letName = ((LetFunction) node).letName;
            try {
                scope.registerVariable(letName, node, scope.resolveType(((LetFunction) node).returnType.representation()).orElse(scope.resolveType("any").get()));
            } catch (TypeCanNotBeReassignedException e) {
                // It's fine, let functions can be overloaded
            }

            Scope letScope = scope.diveIn((Scoped) node);
            ((LetFunction) node).parameters.stream().filter(e -> e instanceof SimpleParameter).forEach(parameter -> {
                String paramName = ((SimpleParameter) parameter).name;
                try {
                    letScope.registerVariable(paramName, parameter, scope.resolveType(((SimpleParameter) parameter).astType.representation()).orElse(scope.resolveType("any").get()));
                } catch (TypeCanNotBeReassignedException e) {
                    log.syntaxError(new SonataSyntaxError(node, "Parameter '" + paramName + "' has been already defined. Found on " + e.initialAssignment()));
                }
            });
            apply(letScope, ((LetFunction) node).body);
        }
    }

    private void registerFields(Scope scope, Node owner, Map<String, Type> fields, SimpleField field) {
        final String typeName = field.astType.representation();
        Optional<Type> refType = scope.resolveType(typeName);
        if (!refType.isPresent()) {
            log.syntaxError(new SonataSyntaxError(owner, "Field '" + field.name + "' refers to a type '" + typeName + "', which does not exist."));
        } else {
            fields.put(field.name, refType.orElse(willBeAny(scope)));
        }
    }

    private void registerMethods(Scope scope, Map<String, FunctionType> methods, LetFunction method) {
        String methodName = method.letName;
        if (method.parameters.stream().anyMatch(p -> !(p instanceof SimpleParameter))) {
            return;
        }

        List<Type> parameters = method.parameters.stream().map(p -> (SimpleParameter) p).map(param -> {
            final String paramTypeName = param.astType.representation();
            Optional<Type> paramType = scope.resolveType(paramTypeName);
            return paramType.orElse(willBeAny(scope));
        }).collect(Collectors.toList());
        Type returnType = scope.resolveType(method.returnType.representation()).orElse(willBeAny(scope));
        methods.put(methodName, new FunctionType(method.definition(), methodName, returnType, parameters));
    }

    private Type willBeAny(Scope scope) {
        return scope.resolveType("any").get();
    }

    @Override
    public String phase() {
        return "LET PROCESSING";
    }
}
