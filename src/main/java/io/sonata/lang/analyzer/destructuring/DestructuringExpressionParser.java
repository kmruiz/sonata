package io.sonata.lang.analyzer.destructuring;

import io.sonata.lang.parser.ast.Node;
import io.sonata.lang.parser.ast.exp.Expression;
import io.sonata.lang.parser.ast.let.LetFunction;
import io.sonata.lang.parser.ast.let.fn.Parameter;

import java.util.List;
import java.util.stream.Stream;

public interface DestructuringExpressionParser {
    Stream<Node> createDestructuringExpression(String parameterName, Parameter parameter);
    Parameter normalizeParameter(String parameterName, Parameter parameter);
    Stream<Expression> generateGuardCondition(String parameterName, Parameter parameter);
}
