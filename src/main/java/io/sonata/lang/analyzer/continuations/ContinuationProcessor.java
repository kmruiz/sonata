/*
 * Copyright (c) 2020 Kevin Mas Ruiz
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package io.sonata.lang.analyzer.continuations;

import io.sonata.lang.analyzer.Processor;
import io.sonata.lang.analyzer.ProcessorIterator;
import io.sonata.lang.analyzer.ProcessorWrapper;
import io.sonata.lang.analyzer.typeSystem.FunctionType;
import io.sonata.lang.analyzer.typeSystem.Scope;
import io.sonata.lang.analyzer.typeSystem.Type;
import io.sonata.lang.parser.ast.Node;
import io.sonata.lang.parser.ast.ScriptNode;
import io.sonata.lang.parser.ast.classes.contracts.Contract;
import io.sonata.lang.parser.ast.classes.entities.EntityClass;
import io.sonata.lang.parser.ast.classes.values.ValueClass;
import io.sonata.lang.parser.ast.exp.*;
import io.sonata.lang.parser.ast.let.LetConstant;
import io.sonata.lang.parser.ast.let.LetFunction;
import io.sonata.lang.parser.ast.requires.RequiresNode;
import io.sonata.lang.parser.ast.type.BasicASTTypeRepresentation;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class ContinuationProcessor implements ProcessorIterator {
    public static Processor processorInstance(Scope scope) {
        return new ProcessorWrapper(scope, "CONTINUATIONS",
                new ContinuationProcessor()
        );
    }

    @Override
    public Node apply(Processor parent, Scope scope, ScriptNode node, List<Node> body) {
        return new ScriptNode(node.log, body, node.currentNode);
    }

    @Override
    public Expression apply(Processor parent, Scope scope, FunctionCall node, Expression receiver, List<Expression> arguments) {
        FunctionCall newFn = new FunctionCall(receiver, arguments, node.expressionType);

        if (scope.inEntityClass() && shouldWaitForAllContinuations(scope, newFn)) {
            return new Continuation(newFn.definition(), newFn, true);
        }

        if (isInferredTypeEntityClass(scope, newFn)) {
            return new Continuation(newFn.definition(), newFn, false);
        }

        if (scope.inEntityClass() && !receiver.representation().contains("some")) {
            return new Continuation(newFn.definition(), newFn, false);
        }

        if (receiver instanceof Continuation) {
            return new Continuation(newFn.definition(), newFn, false);
        }

        return newFn;
    }

    @Override
    public Expression apply(Processor parent, Scope scope, MethodReference node, Expression receiver) {
        return new MethodReference(receiver, node.methodName);
    }

    @Override
    public Node apply(Processor parent, Scope scope, EntityClass node, List<Node> body) {
        return new EntityClass(node.definition, node.name, node.definedFields, node.implementingContracts, body);
    }

    @Override
    public Node apply(Processor parent, Scope scope, ValueClass node, List<Node> body) {
        return new ValueClass(node.definition, node.name, node.definedFields, body);
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
    public Node apply(Processor parent, Scope scope, LetConstant node, Expression body) {
        return new LetConstant(node.definition, node.letName, node.returnType, body);
    }

    @Override
    public Node apply(Processor parent, Scope scope, LetFunction node, Expression body) {
        return new LetFunction(node.letId, node.definition, node.letName, node.parameters, node.returnType, body, node.isAsync, node.isClassLevel);
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

    private boolean shouldWaitForAllContinuations(Scope scope, FunctionCall fc) {
        return fc.receiver instanceof LiteralArray && fc.receiver.representation().endsWith("map");
    }

    private boolean isInferredTypeEntityClass(Scope scope, Expression expression) {
        if (expression instanceof MethodReference) {
            MethodReference ref = (MethodReference) expression;

            if (ref.methodName.equals("some")) {
                return false;
            }

            if (ref.receiver instanceof Atom) {
                Atom receiver = (Atom) ref.receiver;
                if (receiver.kind != Atom.Kind.IDENTIFIER) {
                    return false;
                }

                final Optional<Scope.Variable> variable = scope.resolveVariable(receiver.value);
                return variable.map(v -> v.type.isEntity()).orElse(false);
            }

            if (ref.receiver instanceof MethodReference) {
                return isInferredTypeEntityClass(scope, ref.receiver);
            }

            if (ref.receiver instanceof Continuation) {
                return true;
            }
        }

        if (expression instanceof FunctionCall) {
            final FunctionCall fc = (FunctionCall) expression;
            if (fc.receiver instanceof Atom) {
                final Atom name = (Atom) fc.receiver;

                return scope.resolveVariable(name.value)
                        .filter(variable -> variable.type instanceof FunctionType)
                        .map(variable -> (FunctionType) variable.type)
                        .map(fcType -> fcType.returnType.isEntity())
                        .orElse(false);
            }

            if (fc.receiver instanceof MethodReference) {
                final MethodReference methodReference = ((MethodReference) fc.receiver);
                if (methodReference.receiver instanceof Continuation) {
                    return isInferredTypeEntityClass(scope, fc.receiver);
                }

                Type type = inferTypeOf(scope, methodReference.receiver);
                return Optional.ofNullable(type.methods().get(methodReference.methodName))
                        .filter(e -> e.returnType.isEntity())
                        .isPresent();
            }

            return isInferredTypeEntityClass(scope, fc.receiver);
        }

        if (expression instanceof Atom) {
            return expression.representation().equals("self") && scope.inEntityClass();
        }

        return false;
    }

    private Type inferTypeOf(Scope scope, Expression receiver) {
        if (receiver instanceof Atom) {
            Atom name = (Atom) receiver;
            Optional<Scope.Variable> maybeVariable = scope.resolveVariable(name.value);
            if (maybeVariable.isPresent()) {
                return maybeVariable.map(e -> e.type).orElse(Scope.TYPE_ANY);
            }

            return scope.resolveType(new BasicASTTypeRepresentation(null, name.value))
                    .orElse(Scope.TYPE_ANY);
        }

        return Scope.TYPE_ANY;
    }
}
