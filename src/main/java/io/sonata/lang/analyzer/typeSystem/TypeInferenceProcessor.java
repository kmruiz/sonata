/*
 * Copyright (c) 2020 Kevin Mas Ruiz
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package io.sonata.lang.analyzer.typeSystem;

import io.sonata.lang.analyzer.Processor;
import io.sonata.lang.log.CompilerLog;
import io.sonata.lang.parser.ast.Node;
import io.sonata.lang.parser.ast.ScriptNode;
import io.sonata.lang.parser.ast.classes.entities.EntityClass;
import io.sonata.lang.parser.ast.classes.values.ValueClass;
import io.sonata.lang.parser.ast.exp.*;
import io.sonata.lang.parser.ast.let.LetConstant;
import io.sonata.lang.parser.ast.let.LetFunction;
import io.sonata.lang.parser.ast.type.ASTTypeRepresentation;
import io.sonata.lang.parser.ast.type.BasicASTTypeRepresentation;
import io.sonata.lang.parser.ast.type.EmptyASTTypeRepresentation;

import java.util.List;

import static java.util.stream.Collectors.toList;

public final class TypeInferenceProcessor implements Processor {
    private final CompilerLog log;
    private final Scope rootScope;

    public TypeInferenceProcessor(CompilerLog log, Scope rootScope) {
        this.log = log;
        this.rootScope = rootScope;
    }

    @Override
    public Node apply(Node node) {
        return apply(rootScope, node);
    }

    private Node apply(Scope scope, Node node) {
        final Scope currentScope = scope.diveInIfNeeded(node);

        if (node instanceof ScriptNode) {
            ScriptNode script = (ScriptNode) node;
            List<Node> nodes = script.nodes.stream().map(e -> this.apply(currentScope, e)).collect(toList());
            return new ScriptNode(script.log, nodes, script.currentNode, script.requiresNotifier);
        }

        if (node instanceof EntityClass) {
            EntityClass entityClass = (EntityClass) node;
            List<Node> body = entityClass.body.stream().map(e -> this.apply(currentScope, e)).collect(toList());
            return new EntityClass(entityClass.definition, entityClass.name, entityClass.definedFields, entityClass.implementingContracts, body);
        }

        if (node instanceof ValueClass) {
            ValueClass valueClass  = (ValueClass) node;
            List<Node> body = valueClass.body.stream().map(e -> this.apply(currentScope, e)).collect(toList());
            return new ValueClass(valueClass.definition, valueClass.name, valueClass.definedFields, body);
        }

        if (node instanceof LetConstant) {
            LetConstant constant = (LetConstant) node;
            ASTTypeRepresentation typeRepresentation = constant.returnType;
            Type constantType = null;
            if (typeRepresentation == null || typeRepresentation instanceof EmptyASTTypeRepresentation) {
                constantType = infer(scope, constant.body);
            } else {
                constantType = currentScope.resolveType(typeRepresentation).orElse(Scope.TYPE_ANY);
            }

            currentScope.enrichVariable(constant.letName,constant, constantType);
            return new LetConstant(constant.definition, constant.letName, typeRepresentation, (Expression) apply(currentScope, constant.body));
        }

        if (node instanceof LetFunction) {
            LetFunction fn = (LetFunction) node;
            ASTTypeRepresentation typeRepresentation = fn.returnType;
            Type returnType = null;
            if (typeRepresentation == null || typeRepresentation instanceof EmptyASTTypeRepresentation) {
                returnType = infer(scope, fn.body);
            } else {
                returnType = currentScope.resolveType(typeRepresentation).orElse(Scope.TYPE_ANY);
            }

            final List<Type> paramTypes = fn.parameters.stream().map(e -> Scope.TYPE_ANY).collect(toList());
            currentScope.enrichVariable(fn.letName, fn, new FunctionType(node.definition(), fn.letName, returnType, paramTypes));
            return new LetFunction(fn.letId, fn.definition, fn.letName, fn.parameters, typeRepresentation, (Expression) apply(currentScope, fn.body), false);
        }

        if (node instanceof BlockExpression) {
            BlockExpression blockExpression = (BlockExpression) node;
            List<Expression> expressions = blockExpression.expressions.stream().map(e -> this.apply(currentScope, e)).map(t -> (Expression) t).collect(toList());
            return new BlockExpression(blockExpression.blockId, blockExpression.definition, expressions);
        }

        if (node instanceof FunctionCall) {
            FunctionCall fc = (FunctionCall) node;
            List<Expression> parameters = fc.arguments.stream().map(e -> this.apply(currentScope, e)).map(e -> (Expression) e).collect(toList());

            Type inferredType = infer(scope, fc);
            return new FunctionCall(fc.receiver, parameters, new BasicASTTypeRepresentation(fc.definition(), inferredType.name()));
        }

        if (node instanceof IfElse) {
            IfElse ifElse = (IfElse) node;

            return new IfElse(
                    ifElse.definition,
                    (Expression) apply(currentScope, ifElse.condition),
                    (Expression) apply(currentScope, ifElse.whenTrue),
                    ifElse.whenFalse != null ? (Expression) apply(currentScope, ifElse.whenFalse) : null
            );
        }

        if (node instanceof SimpleExpression) {
            SimpleExpression expr = (SimpleExpression) node;
            return new SimpleExpression((Expression) apply(currentScope, expr.leftSide), expr.operator, (Expression) apply(currentScope, expr.rightSide));
        }

        if (node instanceof Lambda) {
            Lambda lambda = (Lambda) node;
            return new Lambda(lambda.lambdaId, lambda.definition, lambda.parameters, (Expression) apply(scope.diveInIfNeeded(lambda), lambda.body), lambda.isAsync);
        }

        return node;
    }

    public Type infer(Scope scope, Expression expression) {
        if (expression == null) {
            return Scope.TYPE_ANY;
        }

        if (expression instanceof Atom) {
            switch (((Atom) expression).kind) {
                case NUMERIC:
                    return Scope.TYPE_NUMBER;
                case STRING:
                    return Scope.TYPE_STRING;
                case BOOLEAN:
                    return Scope.TYPE_BOOLEAN;
            }

            return scope.resolveVariable(expression.representation()).map(e -> e.type).orElse(Scope.TYPE_ANY);
        }

        if (expression instanceof Lambda) {
            Lambda lambda = (Lambda) expression;
            return scope.resolveType(lambda.type()).orElse(Scope.TYPE_ANY);
        }

        if (expression instanceof FunctionCall) {
            FunctionCall fc = (FunctionCall) expression;
            if (fc.receiver instanceof Atom) {
                Atom fnName = (Atom) fc.receiver;
                return scope.resolveVariable(fnName.value)
                        .filter(var -> var.type instanceof FunctionType)
                        .map(var -> ((FunctionType) var.type).returnType)
                        .orElse(Scope.TYPE_ANY);
            }

            if (fc.receiver instanceof MethodReference) {
                MethodReference methodReference = (MethodReference) fc.receiver;
                Type receiverType = infer(scope, methodReference.receiver);

                FunctionType methodType = receiverType.methods().get(methodReference.methodName);
                if (methodType == null) {
                    return Scope.TYPE_ANY;
                }

                return methodType.returnType;
            }
        }

        if (expression instanceof ArrayAccess) {
            ArrayAccess ac = (ArrayAccess) expression;
            Type receiverType = infer(scope, ac.receiver);
            if (receiverType instanceof ArrayType) {
                ArrayType arrayType = (ArrayType) receiverType;
                return arrayType.references;
            }

            return Scope.TYPE_ANY;
        }

        if (expression instanceof BlockExpression) {
            BlockExpression be = (BlockExpression) expression;
            return infer(scope, be.expressions.get(be.expressions.size() - 1));
        }

        if (expression instanceof PriorityExpression) {
            PriorityExpression pe = (PriorityExpression) expression;
            return infer(scope, pe.expression);
        }

        if (expression instanceof IfElse) {
            IfElse ifElse = (IfElse) expression;
            return infer(scope.diveInIfNeeded(ifElse.whenTrue), ifElse.whenTrue);
        }

        return Scope.TYPE_ANY;
    }

    @Override
    public String phase() {
        return "TYPE INFERENCE";
    }
}
