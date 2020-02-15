/*
 * Copyright (c) 2020 Kevin Mas Ruiz
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package io.sonata.lang.analyzer.typeSystem;

import io.sonata.lang.analyzer.Processor;
import io.sonata.lang.analyzer.ProcessorIterator;
import io.sonata.lang.analyzer.ProcessorWrapper;
import io.sonata.lang.analyzer.typeSystem.exception.TypeCanNotBeReassignedException;
import io.sonata.lang.exception.ParserException;
import io.sonata.lang.exception.SonataSyntaxError;
import io.sonata.lang.log.CompilerLog;
import io.sonata.lang.parser.ast.Node;
import io.sonata.lang.parser.ast.ScriptNode;
import io.sonata.lang.parser.ast.classes.contracts.Contract;
import io.sonata.lang.parser.ast.classes.entities.EntityClass;
import io.sonata.lang.parser.ast.classes.fields.SimpleField;
import io.sonata.lang.parser.ast.classes.values.ValueClass;
import io.sonata.lang.parser.ast.exp.*;
import io.sonata.lang.parser.ast.let.LetConstant;
import io.sonata.lang.parser.ast.let.LetFunction;
import io.sonata.lang.parser.ast.let.fn.SimpleParameter;
import io.sonata.lang.parser.ast.requires.RequiresNode;
import io.sonata.lang.parser.ast.type.BasicASTTypeRepresentation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public final class LetVariableProcessor implements ProcessorIterator {
    private final CompilerLog log;

    public static Processor processorInstance(Scope scope, CompilerLog log) {
        return new ProcessorWrapper(scope, "LET PROCESSING",
                new LetVariableProcessor(log)
        );
    }

    private LetVariableProcessor(CompilerLog log) {
        this.log = log;
    }

    @Override
    public Node apply(Processor parent, Scope scope, ScriptNode node, List<Node> body) {
        return new ScriptNode(node.log, body, node.currentNode);
    }

    @Override
    public Expression apply(Processor parent, Scope scope, FunctionCall node, Expression receiver, List<Expression> arguments) {
        return new FunctionCall(receiver, arguments, node.expressionType);
    }

    @Override
    public Expression apply(Processor parent, Scope scope, MethodReference node, Expression receiver) {
        return new MethodReference(receiver, node.methodName);
    }

    @Override
    public Node apply(Processor parent, Scope classScope, EntityClass entityClass, List<Node> body) {
        Map<String, FunctionType> methods = new HashMap<>();
        Map<String, Type> fields = new HashMap<>();

        String className = entityClass.name;

        entityClass.definedFields.stream().map(f -> (SimpleField) f).forEach(field ->
                registerFields(classScope, entityClass, fields, field)
        );

        entityClass.body.stream().filter(e -> e instanceof LetFunction).map(e -> (LetFunction) e ).forEach(method ->
                registerMethods(classScope, methods, method)
        );

        final Optional<Type> incompleteType = classScope.resolveType(new BasicASTTypeRepresentation(entityClass.definition, className));
        if (!incompleteType.isPresent() || !(incompleteType.get() instanceof EntityClassType)) {
            throw new ParserException(entityClass, "Somehow we didn't manage to pre-register this entity class. Please fill a bug with a sample code.");
        }

        EntityClassType preregisteredClassType = (EntityClassType) incompleteType.get();
        final EntityClassType entityClassType = new EntityClassType(entityClass.definition(), className, fields, preregisteredClassType.contracts, methods);
        List<Type> paramTypes = entityClass.definedFields.stream().map(e -> Scope.TYPE_ANY).collect(Collectors.toList());
        preregisteredClassType.fields.putAll(fields);
        preregisteredClassType.methods.putAll(methods);

        try {
            classScope.registerVariable(className, entityClass, new FunctionType(entityClass.definition, className, entityClassType, paramTypes));
        } catch (TypeCanNotBeReassignedException e) {
            log.syntaxError(new SonataSyntaxError(entityClass, "Redefined entity class " + className + ". It was initially defined in " + e.initialAssignment()));
        }

        return new EntityClass(entityClass.definition, entityClass.name, entityClass.definedFields, entityClass.implementingContracts, body);
    }

    @Override
    public Node apply(Processor parent, Scope classScope, ValueClass valueClass, List<Node> body) {
        Map<String, Type> fields = new HashMap<>();
        Map<String, FunctionType> methods = new HashMap<>();

        String className = valueClass.name;

        valueClass.definedFields.stream().map(f -> (SimpleField) f).forEach(field ->
                registerFields(classScope, valueClass, fields, field)
        );

        valueClass.body.stream().filter(e -> e instanceof LetFunction).map(e -> (LetFunction) e ).forEach(method ->
                registerMethods(classScope, methods, method)
        );

        final Optional<Type> incompleteType = classScope.resolveType(new BasicASTTypeRepresentation(valueClass.definition, className));
        if (!incompleteType.isPresent()) {
            throw new ParserException(valueClass, "Somehow we didn't manage to pre-register this value class. Please fill a bug with a sample code.");
        }

        final ValueClassType vcType = new ValueClassType(valueClass.definition(), className, fields, methods);
        vcType.fields.putAll(fields);
        vcType.methods.putAll(methods);

        List<Type> paramTypes = valueClass.definedFields.stream().map(e -> Scope.TYPE_ANY).collect(Collectors.toList());
        try {
            classScope.registerVariable(className, valueClass, new FunctionType(valueClass.definition, className, vcType, paramTypes));
        } catch (TypeCanNotBeReassignedException e) {
            log.syntaxError(new SonataSyntaxError(valueClass, "Redefined value class " + className + ". It was initially defined in " + e.initialAssignment()));
        }

        return new ValueClass(valueClass.definition, valueClass.name, valueClass.definedFields, body);
    }

    @Override
    public Node apply(Processor parent, Scope scope, Contract node, List<Node> body) {
        return new Contract(node.definition, node.name, body);
    }

    @Override
    public Expression apply(Processor parent, Scope scope, ArrayAccess node, Expression receiver) {
        return new ArrayAccess(receiver, node.index);
    }

    @Override
    public Expression apply(Processor parent, Scope scope, Atom node) {
        return node;
    }

    @Override
    public Expression apply(Processor parent, Scope scope, LiteralArray node, List<Expression> contents) {
        return new LiteralArray(node.definition, contents);
    }

    @Override
    public Expression apply(Processor parent, Scope scope, PriorityExpression node, Expression content) {
        return new PriorityExpression(content);
    }

    @Override
    public Expression apply(Processor parent, Scope scope, Record node, Map<Atom, Expression> values) {
        return new Record(node.definition, values);
    }

    @Override
    public Expression apply(Processor parent, Scope scope, SimpleExpression node, Expression left, Expression right) {
        return new SimpleExpression(left, node.operator, right);
    }

    @Override
    public Expression apply(Processor parent, Scope scope, TypeCheckExpression node) {
        return node;
    }

    @Override
    public Expression apply(Processor parent, Scope scope, ValueClassEquality node, Expression left, Expression right) {
        return new ValueClassEquality(left, right, node.negate);
    }

    @Override
    public Node apply(Processor parent, Scope scope, RequiresNode node) {
        return node;
    }

    @Override
    public Expression apply(Processor parent, Scope scope, TailExtraction node, Expression receiver) {
        return new TailExtraction(receiver, node.fromIndex);
    }

    @Override
    public Expression apply(Processor parent, Scope scope, BlockExpression node, List<Expression> body) {
        return new BlockExpression(node.blockId, node.definition, body);
    }

    @Override
    public Node apply(Processor parent, Scope scope, LetConstant constant, Expression body) {
        try {
            scope.registerVariable(constant.letName, constant, scope.resolveType(constant.returnType).orElse(scope.resolveType(constant.body.type()).orElse(Scope.TYPE_ANY)));
        } catch (TypeCanNotBeReassignedException e) {
            log.syntaxError(new SonataSyntaxError(constant, "Variable '" + constant.letName + "' has been already defined. Found on " + e.initialAssignment()));
        }

        return new LetConstant(constant.definition, constant.letName, constant.returnType, body);
    }

    @Override
    public Node apply(Processor parent, Scope scope, LetFunction node, Expression body) {
        try {
            Type retType = scope.resolveType(node.returnType).orElse(Scope.TYPE_ANY);
            List<Type> paramTypes = node.parameters.stream().map(e -> Scope.TYPE_ANY).collect(Collectors.toList());

            scope.registerVariable(node.letName, node, new FunctionType(node.definition, node.letName, retType, paramTypes));
        } catch (TypeCanNotBeReassignedException e) {
            // It's fine, let functions can be overloaded
        }

        Scope letScope = scope.diveInIfNeeded(node);
        node.parameters.stream().filter(e -> e instanceof SimpleParameter).forEach(parameter -> {
            String paramName = ((SimpleParameter) parameter).name;
            try {
                letScope.registerVariable(paramName, parameter, scope.resolveType(((SimpleParameter) parameter).astTypeRepresentation).orElse(Scope.TYPE_ANY));
            } catch (TypeCanNotBeReassignedException e) {
                log.syntaxError(new SonataSyntaxError(node, "Parameter '" + paramName + "' has been already defined. Found on " + e.initialAssignment()));
            }
        });
        return new LetFunction(node.letId, node.definition, node.letName, node.parameters, node.returnType, node.body, node.isAsync, node.isClassLevel);
    }

    @Override
    public Expression apply(Processor parent, Scope scope, Lambda node, Expression body) {
        return new Lambda(node.lambdaId, node.definition, node.parameters, body, node.isAsync);
    }

    @Override
    public Expression apply(Processor parent, Scope scope, IfElse node, Expression condition, Expression whenTrue, Expression whenFalse) {
        return new IfElse(node.definition, condition, whenTrue, whenFalse);
    }

    @Override
    public Expression apply(Processor parent, Scope scope, Continuation node, Expression body) {
        return new Continuation(node.definition, body, node.fanOut);
    }

    private void registerFields(Scope scope, Node owner, Map<String, Type> fields, SimpleField field) {
        final String typeName = field.astTypeRepresentation.representation();
        Optional<Type> refType = scope.resolveType(field.astTypeRepresentation);
        if (!refType.isPresent()) {
            log.syntaxError(new SonataSyntaxError(owner, "Field '" + field.name + "' refers to a type '" + typeName + "', which does not exist."));
        } else {
            fields.put(field.name, refType.orElse(Scope.TYPE_ANY));
        }
    }

    private void registerMethods(Scope scope, Map<String, FunctionType> methods, LetFunction method) {
        String methodName = method.letName;
        if (method.parameters.stream().anyMatch(p -> !(p instanceof SimpleParameter))) {
            return;
        }

        List<Type> parameters = method.parameters.stream().map(p -> (SimpleParameter) p).map(param -> {
            Optional<Type> paramType = scope.resolveType(param.astTypeRepresentation);
            return paramType.orElse(Scope.TYPE_ANY);
        }).collect(Collectors.toList());
        Type returnType = scope.resolveType(method.returnType).orElse(Scope.TYPE_ANY);
        methods.put(methodName, new FunctionType(method.definition(), methodName, returnType, parameters));
    }
}
