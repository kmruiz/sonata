/*
 * Copyright (c) 2020 Kevin Mas Ruiz
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package io.sonata.lang.analyzer.partials;

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
import io.sonata.lang.parser.ast.let.fn.SimpleParameter;
import io.sonata.lang.parser.ast.requires.RequiresNode;
import io.sonata.lang.parser.ast.type.BasicASTTypeRepresentation;
import io.sonata.lang.source.SourcePosition;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public final class QuestionMarkPartialFunctionProcessor implements ProcessorIterator {
    public static Processor processorInstance(Scope scope) {
        return new ProcessorWrapper(scope, "ANON. PARTIAL FUNCTIONS",
                new QuestionMarkPartialFunctionProcessor()
        );
    }

    @Override
    public Node apply(Processor processor, Scope scope, ScriptNode node, List<Node> body) {
        return new ScriptNode(node.log, body, node.currentNode);
    }

    @Override
    public Expression apply(Processor processor, Scope scope, FunctionCall node, Expression receiver, List<Expression> arguments, Node parent) {
        return processAnonymousParametersOnFunctionCall(receiver, arguments);
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
        return buildLambdaIfNeeded(new PriorityExpression(content));
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
        return new LetConstant(constant.definition, constant.letName, constant.returnType, buildLambdaIfNeeded(body));
    }

    @Override
    public Node apply(Processor processor, Scope scope, LetFunction node, Expression body, Node parent) {
        return new LetFunction(node.letId, node.definition, node.letName, node.parameters, node.returnType, buildLambdaIfNeeded(body), node.isAsync, node.isClassLevel);
    }

    @Override
    public Expression apply(Processor processor, Scope scope, Lambda node, Expression body, Node parent) {
        return new Lambda(node.lambdaId, node.definition, node.parameters, buildLambdaIfNeeded(body), node.isAsync);
    }

    @Override
    public Expression apply(Processor processor, Scope scope, IfElse node, Expression condition, Expression whenTrue, Expression whenFalse, Node parent) {
        return new IfElse(node.definition, condition, whenTrue, whenFalse);
    }

    @Override
    public Expression apply(Processor processor, Scope scope, Continuation node, Expression body, Node parent) {
        return new Continuation(node.definition, body, node.fanOut);
    }

    private Expression processAnonymousParametersOnFunctionCall(Expression receiver, List<Expression> arguments) {
        List<Expression> args = arguments.stream().map(this::buildLambdaIfNeeded).collect(Collectors.toList());
        return new FunctionCall(receiver, args);
    }

    private Expression buildLambdaIfNeeded(Expression expression) {
        ArrayList<SimpleParameter> lambdaParams = new ArrayList<>(4);
        Supplier<String> paramNameSupplier = scopedLambdaParameterNameSupplier(expression.definition(), lambdaParams);
        Expression newExpression = parseExpressionForLambda(expression, paramNameSupplier);

        if (lambdaParams.isEmpty()) {
            return expression;
        }

        return Lambda.synthetic(expression.definition(), lambdaParams, newExpression);
    }

    private Expression parseExpressionForLambda(Expression expression, Supplier<String> paramNameSupplier) {
        if (expression instanceof SimpleExpression) {
            SimpleExpression se = (SimpleExpression) expression;

            Expression left = parseExpressionForLambda(se.leftSide, paramNameSupplier);
            String operator = se.operator;
            Expression right = parseExpressionForLambda(se.rightSide, paramNameSupplier);

            return new SimpleExpression(left, operator, right);
        } else if (expression instanceof Atom) {
            if (Atom.isUnknownAtom(expression)) {
                return new Atom(expression.definition(), paramNameSupplier.get());
            }

            return expression;
        } else if (expression instanceof MethodReference) {
            MethodReference ref = (MethodReference) expression;
            Expression receiver = parseExpressionForLambda(ref.receiver, paramNameSupplier);
            return new MethodReference(receiver, ref.methodName);
        } else if (expression instanceof FunctionCall) {
            FunctionCall fc = (FunctionCall) expression;
            Expression rcv = parseExpressionForLambda(fc.receiver, paramNameSupplier);
            List<Expression> args = fc.arguments.stream().map(e -> parseExpressionForLambda(e, paramNameSupplier)).collect(Collectors.toList());
            return new FunctionCall(rcv, args);
        } else if (expression instanceof PriorityExpression) {
            PriorityExpression expr = (PriorityExpression) expression;
            return new PriorityExpression(parseExpressionForLambda(expr.expression, paramNameSupplier));
        }

        return expression;
    }

    private Supplier<String> scopedLambdaParameterNameSupplier(SourcePosition definition, List<SimpleParameter> lambdaParams) {
        AtomicInteger counter = new AtomicInteger(0);
        char base = 'a';

        return () -> {
            String name = String.valueOf((char) (base + counter.getAndIncrement()));
            lambdaParams.add(new SimpleParameter(definition, name, new BasicASTTypeRepresentation(definition, "any"), SimpleParameter.State.END));

            return name;
        };
    }
}
