/*
 * Copyright (c) 2020 Kevin Mas Ruiz
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package io.sonata.lang.analyzer.destructuring;

import io.sonata.lang.analyzer.Processor;
import io.sonata.lang.analyzer.ProcessorIterator;
import io.sonata.lang.analyzer.ProcessorWrapper;
import io.sonata.lang.analyzer.symbols.SymbolResolver;
import io.sonata.lang.analyzer.typeSystem.Scope;
import io.sonata.lang.parser.ast.Node;
import io.sonata.lang.parser.ast.ScriptNode;
import io.sonata.lang.parser.ast.classes.contracts.Contract;
import io.sonata.lang.parser.ast.classes.entities.EntityClass;
import io.sonata.lang.parser.ast.classes.values.ValueClass;
import io.sonata.lang.parser.ast.exp.*;
import io.sonata.lang.parser.ast.let.LetConstant;
import io.sonata.lang.parser.ast.let.LetFunction;
import io.sonata.lang.parser.ast.let.fn.Parameter;
import io.sonata.lang.parser.ast.let.fn.SimpleParameter;
import io.sonata.lang.parser.ast.requires.RequiresNode;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.sonata.lang.javaext.Lists.append;

public final class DestructuringProcessor implements ProcessorIterator {
    private final DestructuringExpressionParser expressionParsers;

    public static Processor processorInstance(Scope scope, SymbolResolver resolver) {
        return new ProcessorWrapper(scope, "DESTRUCTURING",
                new DestructuringProcessor(resolver, scope)
        );
    }

    private DestructuringProcessor(SymbolResolver resolver, Scope scope) {
        this.expressionParsers = new ComposedDestructuringExpressionParser(
                new ValueClassDestructuringExpressionParser(resolver),
                new ArrayDestructuringExpressionParser(),
                new FunctionOverloadExpressionParser(resolver, scope)
        );
    }

    @Override
    public Node apply(Processor processor, Scope scope, ScriptNode node, List<Node> body) {
        return new ScriptNode(node.log, reduceFunctionsIfAny(body), node.currentNode);
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
        return new EntityClass(entityClass.definition, entityClass.name, entityClass.definedFields, entityClass.implementingContracts, reduceFunctionsIfAny(body));
    }

    @Override
    public Node apply(Processor processor, Scope classScope, ValueClass valueClass, List<Node> body, Node parent) {
        return new ValueClass(valueClass.definition, valueClass.name, valueClass.definedFields, reduceFunctionsIfAny(body));
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
    public Node apply(Processor processor, Scope scope, LetConstant constant, Expression body, Node parent) {
        return new LetConstant(constant.definition, constant.letName, constant.returnType, body);
    }

    @Override
    public Node apply(Processor processor, Scope scope, LetFunction node, Expression body, Node parent) {
        return new LetFunction(node.letId, node.definition, node.letName, node.parameters, node.returnType, body, node.isAsync, node.isClassLevel);
    }

    @Override
    public Expression apply(Processor processor, Scope scope, Lambda node, Expression body, Node parent) {
        return new Lambda(node.lambdaId, node.definition, node.parameters, body, node.isAsync);
    }

    @Override
    public Expression apply(Processor processor, Scope scope, IfElse node, Expression condition, Expression whenTrue, Expression whenFalse, Node parent) {
        return new IfElse(node.definition, condition, whenTrue, whenFalse);
    }

    @Override
    public Expression apply(Processor processor, Scope scope, Continuation node, Expression body, Node parent) {
        return new Continuation(node.definition, body, node.fanOut);
    }

    private List<Node> reduceFunctionsIfAny(List<Node> nodes) {
        Map<String, Optional<NodeAndOrder>> groupedNodes = groupWithOriginalOrder(nodes);
        return groupedNodes.entrySet().stream().sorted(Map.Entry.comparingByKey()).map(Map.Entry::getValue).map(Optional::get).map(children -> {
            if (children.nodes.size() > 1) {
                final LetFunction fn = reduceFunctionList(children.nodes.stream().map(v -> (LetFunction) v).collect(Collectors.toList()));
                return new NodeAndOrder(Collections.singletonList(fn), children.order);
            } else {
                return new NodeAndOrder(children.nodes, children.order);
            }
        })
        .sorted(Comparator.comparingInt(a -> a.order))
        .map(a -> a.nodes.get(0))
        .collect(Collectors.toList());
    }

    private Map<String, Optional<NodeAndOrder>> groupWithOriginalOrder(List<Node> nodes) {
        AtomicInteger currentOrder = new AtomicInteger(0);
        return nodes.stream()
                .map(node -> new NodeAndOrder(Arrays.asList(node), currentOrder.getAndIncrement()))
                .collect(Collectors.groupingBy(nodeAndOrder -> {
                    Node node = nodeAndOrder.nodes.get(0);
                    if (node instanceof LetFunction) {
                        LetFunction fn = (LetFunction) node;
                        return fn.letName;
                    } else {
                        return UUID.randomUUID().toString();
                    }
                }, Collectors.reducing((a, b) -> new NodeAndOrder(append(a.nodes, b.nodes), a.order))));
    }

    private LetFunction reduceFunctionList(List<LetFunction> fns) {
        AtomicInteger idx = new AtomicInteger(0);
        LetFunction master = fns.stream().filter(e -> e.parameters.stream().allMatch(p -> p instanceof SimpleParameter)).findAny().orElse(fns.get(0));

        List<Node> destructuringExpressions = fns
                .stream()
                .flatMap(e -> e.parameters.stream())
                .map(e -> expressionParsers.createDestructuringExpression(parameterNameOf(master, idx.getAndIncrement()), e))
                .filter(Objects::nonNull)
                .flatMap(e -> e)
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(Node::representation))
                .values()
                .stream()
                .flatMap(list -> list.stream().limit(1))
                .collect(Collectors.toList());

        List<Parameter> parameters = master.parameters.stream().map(e -> expressionParsers.normalizeParameter(paramDeclaration(e), e)).collect(Collectors.toList());

        List<Node> all = fns.stream().filter(e -> e.body != null).map(fn -> this.generateGuardedBody(master, fn)).sorted(IfElse::weightedComparison).collect(Collectors.toList());

        return new LetFunction(master.letId, master.definition(), master.letName, parameters, master.returnType, flatten(new BlockExpression(master.definition(), append(destructuringExpressions, all).stream().map(e -> (Expression) e).filter(Objects::nonNull).collect(Collectors.toList()))), false, master.isClassLevel);
    }

    private Expression generateGuardedBody(LetFunction master, LetFunction overload) {
        AtomicInteger idx = new AtomicInteger(0);

        Optional<Expression> guardCondition = overload.parameters.stream()
                .map(e -> expressionParsers.generateGuardCondition(parameterNameOf(master, idx.getAndIncrement()), e)).filter(Objects::nonNull).flatMap(e -> e).filter(Objects::nonNull)
                .sorted(IfElse::weightedComparison)
                .reduce((a, b) -> new SimpleExpression(a, "&&", b));

        if (guardCondition.isPresent()) {
            return new IfElse(overload.definition(), guardCondition.get(), overload.body, null);
        }

        return overload.body;
    }

    private String parameterNameOf(LetFunction master, int idx) {
        return paramDeclaration(master.parameters.get(idx % master.parameters.size()));
    }

    private String paramDeclaration(Parameter p) {
        if (p instanceof SimpleParameter) {
            return ((SimpleParameter) p).name;
        }

        return null;
    }

    private BlockExpression flatten(BlockExpression block) {
        List<Expression> expressions = block.expressions.stream()
                .flatMap(expr -> {
                    if (expr instanceof BlockExpression) {
                        return ((BlockExpression) expr).expressions.stream();
                    }

                    return Stream.of(expr);
                }).collect(Collectors.toList());

        return new BlockExpression(block.definition, expressions);
    }
}

final class NodeAndOrder {
    public final List<Node> nodes;
    public final int order;

    NodeAndOrder(List<Node> nodes, int order) {
        this.nodes = nodes;
        this.order = order;
    }
}