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
import io.sonata.lang.parser.ast.Node;
import io.sonata.lang.parser.ast.ScriptNode;
import io.sonata.lang.parser.ast.classes.contracts.Contract;
import io.sonata.lang.parser.ast.classes.entities.EntityClass;
import io.sonata.lang.parser.ast.classes.values.ValueClass;
import io.sonata.lang.parser.ast.exp.*;
import io.sonata.lang.parser.ast.let.LetConstant;
import io.sonata.lang.parser.ast.let.LetFunction;
import io.sonata.lang.parser.ast.requires.RequiresNode;
import io.sonata.lang.parser.ast.type.ASTTypeRepresentation;
import io.sonata.lang.parser.ast.type.EmptyASTTypeRepresentation;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

public final class TypeInferenceProcessor implements ProcessorIterator {
    public static Processor processorInstance(Scope scope) {
        return new ProcessorWrapper(scope, "CLASS PROCESSING",
                new TypeInferenceProcessor()
        );
    }

    @Override
    public Node apply(Processor processor, Scope scope, ScriptNode node, List<Node> body) {
        return new ScriptNode(node.log, body, node.currentNode);
    }

    @Override
    public Expression apply(Processor processor, Scope scope, FunctionCall node, Expression receiver, List<Expression> arguments, Node parent) {
        Type inferredType = infer(scope, node);
        return new FunctionCall(receiver, arguments, new ASTTypeReference(inferredType));
    }

    @Override
    public Expression apply(Processor processor, Scope scope, MethodReference node, Expression receiver, Node parent) {
        return new MethodReference(receiver, node.methodName);
    }

    @Override
    public Node apply(Processor processor, Scope classScope, EntityClass entityClass, List<Node> body, Node parent) {
        return new EntityClass(entityClass.definition, entityClass.name, entityClass.definedFields, entityClass.implementingContracts, body);
    }

    @Override
    public Node apply(Processor processor, Scope classScope, ValueClass valueClass, List<Node> body, Node parent) {
        return new ValueClass(valueClass.definition, valueClass.name, valueClass.definedFields, body);
    }

    @Override
    public Node apply(Processor processor, Scope scope, Contract node, List<Node> body, Node parent) {
        return new Contract(node.definition, node.name, body, node.extensions);
    }

    @Override
    public Expression apply(Processor processor, Scope scope, ArrayAccess node, Expression receiver, Node parent) {
        return new ArrayAccess(receiver, node.index);
    }

    @Override
    public Expression apply(Processor processor, Scope scope, Atom node, Node parent) {
        return new Atom(node.definition, node.value, new ASTTypeReference(inferAtom(scope, node)));
    }

    @Override
    public Expression apply(Processor processor, Scope scope, LiteralArray node, List<Expression> contents, Node parent) {
        return new LiteralArray(node.definition, contents);
    }

    @Override
    public Expression apply(Processor processor, Scope scope, PriorityExpression node, Expression content, Node parent) {
        return new PriorityExpression(content);
    }

    @Override
    public Expression apply(Processor processor, Scope scope, Record node, Map<Atom, Expression> values, Node parent) {
        return new Record(node.definition, values);
    }

    @Override
    public Expression apply(Processor processor, Scope scope, SimpleExpression node, Expression left, Expression right, Node parent) {
        return new SimpleExpression(left, node.operator, right);
    }

    @Override
    public Expression apply(Processor processor, Scope scope, TypeCheckExpression node, Node parent) {
        return node;
    }

    @Override
    public Expression apply(Processor processor, Scope scope, ValueClassEquality node, Expression left, Expression right, Node parent) {
        return new ValueClassEquality(left, right, node.negate);
    }

    @Override
    public Node apply(Processor processor, Scope scope, RequiresNode node, Node parent) {
        return node;
    }

    @Override
    public Expression apply(Processor processor, Scope scope, TailExtraction node, Expression receiver, Node parent) {
        return new TailExtraction(receiver, node.fromIndex);
    }

    @Override
    public Expression apply(Processor processor, Scope scope, BlockExpression node, List<Expression> body, Node parent) {
        return new BlockExpression(node.blockId, node.definition, body);
    }

    @Override
    public Node apply(Processor processor, Scope scope, LetConstant constant, Expression body, Node parent) {
        ASTTypeRepresentation typeRepresentation = constant.returnType;
        Type constantType = null;
        if (typeRepresentation == null || typeRepresentation instanceof EmptyASTTypeRepresentation) {
            constantType = infer(scope, constant.body);
        } else {
            constantType = scope.resolveType(typeRepresentation).orElse(Scope.TYPE_ANY);
        }

        scope.enrichVariable(constant.letName, constant, constantType);
        return new LetConstant(constant.definition, constant.letName, constant.returnType, body);
    }

    @Override
    public Node apply(Processor processor, Scope scope, LetFunction node, Expression body, Node parent) {
        ASTTypeRepresentation typeRepresentation = node.returnType;

        Type returnType = null;
        if (typeRepresentation == null || typeRepresentation instanceof EmptyASTTypeRepresentation) {
            returnType = infer(scope, body);
        } else {
            returnType = scope.resolveType(typeRepresentation).orElse(Scope.TYPE_ANY);
        }

        final List<Type> paramTypes = node.parameters.stream().map(e -> Scope.TYPE_ANY).collect(toList());
        scope.enrichVariable(node.letName, node, new FunctionType(node.definition(), node.letName, returnType, paramTypes));
        return new LetFunction(node.letId, node.definition, node.letName, node.parameters, new ASTTypeReference(returnType), body, node.isAsync, node.isClassLevel);
    }

    @Override
    public Expression apply(Processor processor, Scope scope, Lambda node, Expression body, Node parent) {
        return new Lambda(node.lambdaId, node.definition, node.parameters, body, node.isAsync, new ASTTypeReference(infer(scope, node)));
    }

    @Override
    public Expression apply(Processor processor, Scope scope, IfElse node, Expression condition, Expression whenTrue, Expression whenFalse, Node parent) {
        return new IfElse(node.definition, condition, whenTrue, whenFalse);
    }

    @Override
    public Expression apply(Processor processor, Scope scope, Continuation node, Expression body, Node parent) {
        return new Continuation(node.definition, body, node.fanOut);
    }

    public Type infer(Scope scope, Expression expression) {
        if (expression == null) {
            return Scope.TYPE_ANY;
        }

        if (expression instanceof Atom) {
            return inferAtom(scope, (Atom) expression);
        }

        if (expression instanceof Lambda) {
            Lambda lambda = (Lambda) expression;
            Expression body = lambda.body;

            List<Type> paramTypes = lambda.parameters.stream().map(e -> Scope.TYPE_ANY).collect(toList());
            Type bodyType = infer(scope, body);

            return new FunctionType(lambda.definition, "<anonymous>", bodyType, paramTypes);
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

    private Type inferAtom(Scope scope, Atom atom) {
        switch (atom.kind) {
            case STRING: return Scope.TYPE_STRING;
            case BOOLEAN: return Scope.TYPE_BOOLEAN;
            case NUMERIC: return Scope.TYPE_NUMBER;
            case UNKNOWN: return Scope.TYPE_ANY;
            case IDENTIFIER: return scope.resolveVariable(atom.value).map(e -> e.type).orElse(Scope.TYPE_ANY);
        }

        return Scope.TYPE_ANY;
    }
}
