package io.sonata.lang.analyzer.destructuring;

import io.sonata.lang.analyzer.symbols.SymbolResolver;
import io.sonata.lang.parser.ast.Node;
import io.sonata.lang.parser.ast.classes.values.ValueClass;
import io.sonata.lang.parser.ast.exp.*;
import io.sonata.lang.parser.ast.let.fn.ExpressionParameter;
import io.sonata.lang.parser.ast.let.fn.Parameter;

import java.util.function.Function;
import java.util.stream.Stream;

public final class FunctionOverloadExpressionParser implements DestructuringExpressionParser {
    private final SymbolResolver resolver;

    public FunctionOverloadExpressionParser(SymbolResolver resolver) {
        this.resolver = resolver;
    }

    private final class ExpressionParameterTouchPoint {
        public final Expression expression;

        private ExpressionParameterTouchPoint(Expression expression) {
            this.expression = expression;
        }
    }

    @Override
    public Stream<Node> createDestructuringExpression(String parameterName, Parameter parameter) {
        return Stream.empty();
    }

    @Override
    public Parameter normalizeParameter(String parameterName, Parameter parameter) {
        return parameter;
    }

    @Override
    public Stream<Expression> generateGuardCondition(String parameterName, Parameter parameter) {
        return whenIsExpressionParameter(parameter, tp -> {
            if (tp.expression instanceof Atom) {
                return Stream.of(new SimpleExpression(new Atom(tp.expression.definition(), parameterName), "===", tp.expression));
            }

            return Stream.of(tp.expression);
        });
    }

    private <T> T whenIsExpressionParameter(Parameter parameter, Function<ExpressionParameterTouchPoint, T> fn) {
        if (parameter instanceof ExpressionParameter) {
            Expression expr = ((ExpressionParameter) parameter).expression;
            boolean isArray = (expr instanceof LiteralArray);
            boolean isValueClass = (expr instanceof FunctionCall) && resolver.isSymbolOfType(((FunctionCall) expr).receiver.representation(), ValueClass.class);

            if (!isArray && !isValueClass) {
                return fn.apply(new ExpressionParameterTouchPoint(expr));
            }
        }

        return null;
    }
}
