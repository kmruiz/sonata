package io.sonata.lang.analyzer.destructuring;

import io.sonata.lang.analyzer.Processor;
import io.sonata.lang.analyzer.symbols.SymbolResolver;
import io.sonata.lang.parser.ast.Node;
import io.sonata.lang.parser.ast.ScriptNode;
import io.sonata.lang.parser.ast.classes.values.ValueClass;
import io.sonata.lang.parser.ast.exp.*;
import io.sonata.lang.parser.ast.let.LetConstant;
import io.sonata.lang.parser.ast.let.LetFunction;
import io.sonata.lang.parser.ast.let.fn.ExpressionParameter;
import io.sonata.lang.parser.ast.let.fn.Parameter;
import io.sonata.lang.parser.ast.let.fn.SimpleParameter;
import io.sonata.lang.parser.ast.type.BasicType;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.sonata.lang.javaext.Lists.append;

public class ValueClassDestructuringProcessor implements Processor {
    private static final class ValueClassParameterTouchPoint {
        public final FunctionCall functionCall;
        public final ValueClass valueClass;

        private ValueClassParameterTouchPoint(FunctionCall functionCall, ValueClass valueClass) {
            this.functionCall = functionCall;
            this.valueClass = valueClass;
        }
    }

    private final SymbolResolver resolver;

    public ValueClassDestructuringProcessor(SymbolResolver resolver) {
        this.resolver = resolver;
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
        var destructuringExpressions = master.parameters.stream().map(this::createDestructuringExpression).filter(Objects::nonNull).flatMap(e -> e).filter(Objects::nonNull).collect(Collectors.toList());
        var parameters = master.parameters.stream().map(this::normalizeParameter).collect(Collectors.toList());

        List<Node> all = fns.stream().map(this::generateGuardedBody).sorted((a, b) -> a instanceof IfElse ? -1 : 1).collect(Collectors.toList());

        return new LetFunction(master.letName, parameters, master.returnType, new BlockExpression(append(destructuringExpressions, all).stream().filter(Objects::nonNull).collect(Collectors.toList())));
    }

    private Expression generateGuardedBody(LetFunction overload) {
        var guardCondition = overload.parameters.stream().map(this::generateGuardCondition).filter(Objects::nonNull).flatMap(e -> e).filter(Objects::nonNull).reduce((a, b) -> new SimpleExpression(a, "&&", b));
        if (guardCondition.isPresent()) {
            return new IfElse(guardCondition.get(), overload.body, null);
        }

        return overload.body;
    }

    private Stream<Expression> generateGuardCondition(Parameter parameter) {
        var argIdx = new AtomicInteger(-1);
        return whenIsValueClassParameter(parameter, tp -> tp.functionCall.arguments.stream().map(arg -> {
            var field = argIdx.incrementAndGet();

            if (arg instanceof Atom) {
                var atom = ((Atom) arg);
                if (atom.type != Atom.Type.IDENTIFIER) {
                    return new SimpleExpression(new Atom(tp.valueClass.definedFields.get(field).name()), "===", arg);
                }
            } else if (arg instanceof Expression) {
                return arg;
            }

            return null;
        }));
    }

    private Stream<Node> createDestructuringExpression(Parameter parameter) {
        var argIdx = new AtomicInteger(-1);
        return whenIsValueClassParameter(parameter, tp -> tp.functionCall.arguments.stream().map(arg -> {
            var field = argIdx.incrementAndGet();
            var fieldName = tp.valueClass.definedFields.get(field).name();

            return new LetConstant(fieldName, null, new SimpleExpression(new Atom(tp.valueClass.name), ".", new Atom(fieldName)));
        }));
    }

    private Parameter normalizeParameter(Parameter parameter) {
        Parameter newParam = whenIsValueClassParameter(parameter, tp -> new SimpleParameter(tp.valueClass.name, new BasicType(tp.valueClass.name), SimpleParameter.State.END));
        if (newParam != null) {
            return newParam;
        }

        return parameter;
    }

    private <T> T whenIsValueClassParameter(Parameter parameter, Function<ValueClassParameterTouchPoint, T> fn) {
        if (parameter instanceof ExpressionParameter) {
            if (((ExpressionParameter) parameter).expression instanceof FunctionCall) {
                var receiver = ((FunctionCall) ((ExpressionParameter) parameter).expression).receiver;
                if (receiver instanceof Atom) {
                    var receiverName = ((Atom) receiver).value;
                    if (resolver.isSymbolOfType(receiverName, ValueClass.class)) {
                        return fn.apply(new ValueClassParameterTouchPoint(
                                (FunctionCall) ((ExpressionParameter) parameter).expression,
                                resolver.resolve(receiverName, ValueClass.class))
                        );
                    }
                }
            }
        }

        return null;
    }

}
