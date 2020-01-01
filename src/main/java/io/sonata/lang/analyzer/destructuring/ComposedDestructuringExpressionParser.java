package io.sonata.lang.analyzer.destructuring;

import io.sonata.lang.parser.ast.Node;
import io.sonata.lang.parser.ast.exp.Expression;
import io.sonata.lang.parser.ast.let.fn.Parameter;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public final class ComposedDestructuringExpressionParser implements DestructuringExpressionParser {
    private final List<DestructuringExpressionParser> parsers;

    public ComposedDestructuringExpressionParser(DestructuringExpressionParser... parsers) {
        this.parsers = Arrays.asList(parsers);
    }

    @Override
    public Stream<Node> createDestructuringExpression(String parameterName, Parameter parameter) {
        return this.parsers.parallelStream().flatMap(p -> p.createDestructuringExpression(parameterName, parameter));
    }

    @Override
    public Parameter normalizeParameter(String parameterName, Parameter parameter) {
        return this.parsers.parallelStream().map(p -> p.normalizeParameter(parameterName, parameter)).filter(Objects::nonNull).findFirst().get();
    }

    @Override
    public Stream<Expression> generateGuardCondition(String parameterName, Parameter parameter) {
        return this.parsers.parallelStream().flatMap(p -> p.generateGuardCondition(parameterName, parameter));
    }
}
