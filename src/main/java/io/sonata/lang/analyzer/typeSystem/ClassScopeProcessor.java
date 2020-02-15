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
import io.sonata.lang.exception.SonataSyntaxError;
import io.sonata.lang.log.CompilerLog;
import io.sonata.lang.parser.ast.Node;
import io.sonata.lang.parser.ast.Scoped;
import io.sonata.lang.parser.ast.ScriptNode;
import io.sonata.lang.parser.ast.classes.contracts.Contract;
import io.sonata.lang.parser.ast.classes.entities.EntityClass;
import io.sonata.lang.parser.ast.classes.values.ValueClass;
import io.sonata.lang.parser.ast.exp.*;
import io.sonata.lang.parser.ast.let.LetConstant;
import io.sonata.lang.parser.ast.let.LetFunction;
import io.sonata.lang.parser.ast.requires.RequiresNode;

import java.util.*;
import java.util.stream.Collectors;

public final class ClassScopeProcessor implements ProcessorIterator {
    private final CompilerLog log;

    public static Processor processorInstance(Scope scope, CompilerLog log) {
        return new ProcessorWrapper(scope, "CLASS PROCESSING",
                new ClassScopeProcessor(log)
        );
    }

    private ClassScopeProcessor(CompilerLog log) {
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
        final String className = entityClass.name;
        try {
            List<ContractType> contracts = entityClass.implementingContracts.stream().map(ct -> {
                Optional<Type> contractType = classScope.resolveType(ct);
                if (!contractType.isPresent()) {
                    log.syntaxError(new SonataSyntaxError(ct, "Can not implement a contract that has not been defined."));
                    return null;
                } else {
                    return (ContractType) contractType.get();
                }
            }).filter(Objects::nonNull).collect(Collectors.toList());

            final EntityClassType type = new EntityClassType(entityClass.definition(), className, new HashMap<>(), contracts, new HashMap<>());
            classScope.registerType(className, type);
            classScope.registerVariable("self", entityClass, type);
        } catch (TypeCanNotBeReassignedException e) {
            log.syntaxError(new SonataSyntaxError(entityClass, "Entity classes can not be redefined, but '" + className + "' is defined at least twice. Found on " + e.initialAssignment()));
        }

        return new EntityClass(entityClass.definition, entityClass.name, entityClass.definedFields, entityClass.implementingContracts, body);
    }

    @Override
    public Node apply(Processor parent, Scope classScope, ValueClass valueClass, List<Node> body) {
        final String className = valueClass.name;
        try {
            final ValueClassType type = new ValueClassType(valueClass.definition(), className, new HashMap<>(), new HashMap<>());
            classScope.registerType(className, type);
            classScope.registerVariable("self", valueClass, type);
        } catch (TypeCanNotBeReassignedException e) {
            log.syntaxError(new SonataSyntaxError(valueClass, "Value classes can not be redefined, but " + className + " is defined at least twice."));
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
        return new LetConstant(constant.definition, constant.letName, constant.returnType, body);
    }

    @Override
    public Node apply(Processor parent, Scope scope, LetFunction node, Expression body) {
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
}
