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
import io.sonata.lang.exception.SonataSyntaxError;
import io.sonata.lang.log.CompilerLog;
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

import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class EqualitySpecializationProcessor implements ProcessorIterator {
    private final CompilerLog log;

    public static Processor processorInstance(Scope scope, CompilerLog log) {
        return new ProcessorWrapper(scope, "EQUALITY SPECIALIZATION",
                new EqualitySpecializationProcessor(log)
        );
    }

    private EqualitySpecializationProcessor(CompilerLog log) {
        this.log = log;
    }

    @Override
    public Node apply(Processor processor, Scope scope, ScriptNode node, List<Node> body) {
        return new ScriptNode(node.log, body, node.currentNode);
    }

    @Override
    public Expression apply(Processor processor, Scope scope, FunctionCall node, Expression receiver, List<Expression> arguments, Node parent) {
        return new FunctionCall(receiver, arguments, node.expressionType);
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
        return node;
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
        if (node.operator.equals("==") || node.operator.equals("!=")) {
            if (!isValidEquality(left, right)) {
                log.syntaxError(new SonataSyntaxError(node, "Comparing unrelated types: " + node.leftSide.type().representation() + " " + node.operator + " " + node.rightSide.type().representation()));
                return new SimpleExpression(left, node.operator, right);
            }

            final ASTTypeRepresentation typeOfComparison = left.type();
            final Optional<Type> maybeType = scope.resolveType(typeOfComparison);
            if (maybeType.isPresent()) {
                final Type type = maybeType.get();
                if (isNotPrimitiveValue(type)) {
                    return new ValueClassEquality(left, right, node.operator.equals("!="));
                }
            }
        }

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
        return new LetConstant(constant.definition, constant.letName, constant.returnType, body);
    }

    @Override
    public Node apply(Processor processor, Scope scope, LetFunction node, Expression body, Node parent) {
        return new LetFunction(node.letId, node.definition, node.letName, node.parameters, node.returnType, body, node.isAsync, node.isClassLevel);
    }

    @Override
    public Expression apply(Processor processor, Scope scope, Lambda node, Expression body, Node parent) {
        return new Lambda(node.lambdaId, node.definition, node.parameters, body, node.isAsync, node.typeRepresentation);
    }

    @Override
    public Expression apply(Processor processor, Scope scope, IfElse node, Expression condition, Expression whenTrue, Expression whenFalse, Node parent) {
        return new IfElse(node.definition, condition, whenTrue, whenFalse);
    }

    @Override
    public Expression apply(Processor processor, Scope scope, Continuation node, Expression body, Node parent) {
        return new Continuation(node.definition, body, node.fanOut);
    }

    private boolean isNotPrimitiveValue(Type type) {
        return type.isValue() && type != Scope.TYPE_NUMBER;
    }

    private boolean isValidEquality(Expression a, Expression b) {
        return a.type().representation().equals(b.type().representation()) || a.type().representation().equals("any") || b.type().representation().equals("any");
    }
}
