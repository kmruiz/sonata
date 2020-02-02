/*
 * Copyright (c) 2020 Kevin Mas Ruiz
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package io.sonata.lang.analyzer.destructuring;

import io.sonata.lang.analyzer.Processor;
import io.sonata.lang.analyzer.symbols.SymbolResolver;
import io.sonata.lang.parser.ast.Node;
import io.sonata.lang.parser.ast.ScriptNode;
import io.sonata.lang.parser.ast.classes.entities.EntityClass;
import io.sonata.lang.parser.ast.classes.values.ValueClass;
import io.sonata.lang.parser.ast.exp.BlockExpression;
import io.sonata.lang.parser.ast.exp.Expression;
import io.sonata.lang.parser.ast.exp.IfElse;
import io.sonata.lang.parser.ast.exp.SimpleExpression;
import io.sonata.lang.parser.ast.let.LetFunction;
import io.sonata.lang.parser.ast.let.fn.Parameter;
import io.sonata.lang.parser.ast.let.fn.SimpleParameter;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.sonata.lang.javaext.Lists.append;

public final class DestructuringProcessor implements Processor {
    private final DestructuringExpressionParser expressionParsers;

    public DestructuringProcessor(SymbolResolver resolver) {
        this.expressionParsers = new ComposedDestructuringExpressionParser(
                new ValueClassDestructuringExpressionParser(resolver),
                new ArrayDestructuringExpressionParser(),
                new FunctionOverloadExpressionParser(resolver)
        );
    }

    @Override
    public Node apply(Node node) {
        if (node instanceof ScriptNode) {
            return parse((ScriptNode) node);
        }

        return node;
    }

    private Node parse(ScriptNode node) {
        List<Node> nodes = node.nodes.stream().map(e -> {
            if (e instanceof ValueClass) {
                ValueClass vc = (ValueClass) e;
                return new ValueClass(vc.definition, vc.name, vc.definedFields, reduceFunctionsIfAny(vc.body));
            }

            if (e instanceof EntityClass) {
                EntityClass entity = (EntityClass) e;
                return new EntityClass(entity.definition, entity.name, entity.definedFields, entity.implementingContracts, reduceFunctionsIfAny(entity.body));
            }

            return e;
        }).collect(Collectors.toList());

        return new ScriptNode(node.log, reduceFunctionsIfAny(nodes), node.currentNode, node.requiresNotifier);
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

        List<Node> all = fns.stream().map(fn -> this.generateGuardedBody(master, fn)).sorted(IfElse::weightedComparison).collect(Collectors.toList());

        return new LetFunction(master.letId, master.definition(), master.letName, parameters, master.returnType, flatten(new BlockExpression(master.definition(), append(destructuringExpressions, all).stream().map(e -> (Expression) e).filter(Objects::nonNull).collect(Collectors.toList()))), false);
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
    @Override
    public String phase() {
        return "DESTRUCTURING";
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