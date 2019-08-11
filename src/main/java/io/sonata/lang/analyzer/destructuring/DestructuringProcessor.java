package io.sonata.lang.analyzer.destructuring;

import io.sonata.lang.analyzer.Processor;
import io.sonata.lang.analyzer.symbols.SymbolResolver;
import io.sonata.lang.parser.ast.Node;
import io.sonata.lang.parser.ast.ScriptNode;
import io.sonata.lang.parser.ast.exp.BlockExpression;
import io.sonata.lang.parser.ast.exp.IfElse;
import io.sonata.lang.parser.ast.let.LetFunction;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.sonata.lang.javaext.Lists.append;

public class DestructuringProcessor implements Processor {
    private final ValueClassDestructuringExpressionParser valueClassParser;

    public DestructuringProcessor(SymbolResolver resolver) {
        this.valueClassParser = new ValueClassDestructuringExpressionParser(resolver);
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
        var currentOrder = new AtomicInteger(0);
        var groupedNodes = nodes.stream().collect(Collectors.groupingBy(node -> (node instanceof LetFunction) ? ((LetFunction) node).letName : String.valueOf(currentOrder.incrementAndGet())));

        return groupedNodes.entrySet().stream().sorted(Comparator.comparing(Map.Entry::getKey)).map(Map.Entry::getValue).map(children -> {
            if (children.size() > 1) {
                return reduceFunctionList(children.stream().map(v -> (LetFunction) v).collect(Collectors.toList()));
            } else {
                return children.get(0);
            }
        });
    }

    private LetFunction reduceFunctionList(List<LetFunction> fns) {
        var master = fns.get(0);
        var destructuringExpressions = master.parameters.stream().map(valueClassParser::createDestructuringExpression).filter(Objects::nonNull).flatMap(e -> e).filter(Objects::nonNull).collect(Collectors.toList());
        var parameters = master.parameters.stream().map(valueClassParser::normalizeParameter).collect(Collectors.toList());

        List<Node> all = fns.stream().map(valueClassParser::generateGuardedBody).sorted((a, b) -> a instanceof IfElse ? -1 : 1).collect(Collectors.toList());

        return new LetFunction(master.letName, parameters, master.returnType, new BlockExpression(append(destructuringExpressions, all).stream().filter(Objects::nonNull).collect(Collectors.toList())));
    }
}
