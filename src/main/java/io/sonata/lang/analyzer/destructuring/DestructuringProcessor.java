package io.sonata.lang.analyzer.destructuring;

import io.sonata.lang.analyzer.Processor;
import io.sonata.lang.analyzer.symbols.SymbolResolver;
import io.sonata.lang.parser.ast.Node;
import io.sonata.lang.parser.ast.ScriptNode;
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

public class DestructuringProcessor implements Processor {
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
        return new ScriptNode(reduceFunctionsIfAny(node.nodes).collect(Collectors.toList()), node.currentNode, node.requiresNotifier);
    }

    private Stream<Node> reduceFunctionsIfAny(List<Node> nodes) {
        AtomicInteger currentOrder = new AtomicInteger(0);
        Map<String, List<Node>> groupedNodes = nodes.stream().collect(Collectors.groupingBy(node -> (node instanceof LetFunction) ? ((LetFunction) node).letName : String.valueOf(currentOrder.incrementAndGet())));

        return groupedNodes.entrySet().stream().sorted(Map.Entry.comparingByKey()).map(Map.Entry::getValue).map(children -> {
            if (children.size() > 1) {
                return reduceFunctionList(children.stream().map(v -> (LetFunction) v).collect(Collectors.toList()));
            } else {
                return children.get(0);
            }
        });
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

        return new LetFunction(master.definition(), master.letName, parameters, master.returnASTType, new BlockExpression(master.body.definition(), append(destructuringExpressions, all).stream().map(e -> (Expression) e).filter(Objects::nonNull).collect(Collectors.toList())));
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

    @Override
    public String phase() {
        return "DESTRUCTURING";
    }
}
