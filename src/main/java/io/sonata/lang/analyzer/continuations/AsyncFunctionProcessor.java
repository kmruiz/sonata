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
import io.sonata.lang.analyzer.typeSystem.Scope;
import io.sonata.lang.parser.ast.Node;
import io.sonata.lang.parser.ast.ScriptNode;
import io.sonata.lang.parser.ast.classes.contracts.Contract;
import io.sonata.lang.parser.ast.classes.entities.EntityClass;
import io.sonata.lang.parser.ast.classes.values.ValueClass;
import io.sonata.lang.parser.ast.exp.*;
import io.sonata.lang.parser.ast.let.LetConstant;
import io.sonata.lang.parser.ast.let.LetFunction;
import io.sonata.lang.parser.ast.requires.RequiresNode;

import java.util.List;
import java.util.Map;

public final class AsyncFunctionProcessor implements ProcessorIterator {
    public static Processor processorInstance(Scope scope) {
        return new ProcessorWrapper(scope, "ASYNC FUNCTIONS", new AsyncFunctionProcessor());
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
    public Node apply(Processor processor, Scope scope, EntityClass node, List<Node> body, Node parent) {
        return new EntityClass(node.definition, node.name, node.definedFields, node.implementingContracts, body);
    }

    @Override
    public Node apply(Processor processor, Scope scope, ValueClass node, List<Node> body, Node parent) {
        return new ValueClass(node.definition, node.name, node.definedFields, body);
    }

    @Override
    public Node apply(Processor processor, Scope scope, Contract node, List<Node> body, Node parent) {
        return new Contract(node.definition, node.name, body);
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
    public Node apply(Processor processor, Scope scope, LetConstant node, Expression body, Node parent) {
        return new LetConstant(node.definition, node.letName, node.returnType, body);
    }

    @Override
    public Node apply(Processor processor, Scope scope, LetFunction node, Expression body, Node parent) {
        final boolean isAsync = hasContinuations(body);
        return new LetFunction(node.letId, node.definition, node.letName, node.parameters, node.returnType, body, isAsync, node.isClassLevel);
    }

    @Override
    public Expression apply(Processor processor, Scope scope, Lambda node, Expression body, Node parent) {
        final boolean isAsync = hasContinuations(body);
        return new Lambda(node.lambdaId, node.definition, node.parameters, body, isAsync);
    }

    @Override
    public Expression apply(Processor processor, Scope scope, IfElse node, Expression condition, Expression whenTrue, Expression whenFalse, Node parent) {
        return new IfElse(node.definition, condition, whenTrue, whenFalse);
    }

    @Override
    public Expression apply(Processor processor, Scope scope, Continuation node, Expression body, Node parent) {
        return new Continuation(node.definition, body, node.fanOut);
    }

    private boolean hasContinuations(Expression node) {
        if (node == null) {
            return false;
        }

        if (node instanceof Continuation) {
            return true;
        }

        if (node instanceof FunctionCall) {
            FunctionCall fc = (FunctionCall) node;
            return hasContinuations(fc.receiver) || fc.arguments.stream().anyMatch(this::hasContinuations);
        }

        if (node instanceof MethodReference) {
            MethodReference ref = (MethodReference) node;
            return ref.receiver instanceof Continuation;
        }

        if (node instanceof BlockExpression) {
            BlockExpression bc = (BlockExpression) node;
            return bc.expressions.stream().anyMatch(this::hasContinuations);
        }

        if (node instanceof LetConstant) {
            LetConstant lc = (LetConstant) node;
            return hasContinuations(lc.body);
        }

        if (node instanceof LetFunction) {
            LetFunction lf = (LetFunction) node;
            return hasContinuations(lf.body);
        }

        if (node instanceof Lambda) {
            Lambda ld = (Lambda) node;
            return hasContinuations(ld.body);
        }

        if (node instanceof IfElse) {
            IfElse ie = (IfElse) node;
            return hasContinuations(ie.whenFalse) || hasContinuations(ie.whenTrue) || hasContinuations(ie.condition);
        }

        return false;
    }
}
