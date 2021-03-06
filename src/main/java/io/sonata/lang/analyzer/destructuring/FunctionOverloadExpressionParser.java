/*
 * Copyright (c) 2020 Kevin Mas Ruiz
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package io.sonata.lang.analyzer.destructuring;

import io.sonata.lang.analyzer.symbols.SymbolResolver;
import io.sonata.lang.analyzer.typeSystem.Scope;
import io.sonata.lang.analyzer.typeSystem.Type;
import io.sonata.lang.parser.ast.Node;
import io.sonata.lang.parser.ast.classes.values.ValueClass;
import io.sonata.lang.parser.ast.exp.*;
import io.sonata.lang.parser.ast.let.fn.ExpressionParameter;
import io.sonata.lang.parser.ast.let.fn.Parameter;
import io.sonata.lang.parser.ast.let.fn.SimpleParameter;

import java.util.function.Function;
import java.util.stream.Stream;

public final class FunctionOverloadExpressionParser implements DestructuringExpressionParser {
    private final SymbolResolver resolver;
    private final Scope scope;

    public FunctionOverloadExpressionParser(SymbolResolver resolver, Scope scope) {
        this.resolver = resolver;
        this.scope = scope;
    }

    private final class ExpressionParameterTouchPoint {
        public final Expression expression;

        private ExpressionParameterTouchPoint(Expression expression) {
            this.expression = expression;
        }
    }

    private final class TypedParameterTouchPoint {
        public final Type type;

        private TypedParameterTouchPoint(Type type) {
            this.type = type;
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
        Stream<Expression> result = whenIsExpressionParameter(parameter, tp -> {
            if (tp.expression instanceof Atom) {
                return Stream.of(new SimpleExpression(new Atom(tp.expression.definition(), parameterName, tp.expression.type()), "===", tp.expression));
            }

            return Stream.of(tp.expression);
        });

        if (result == null) {
            return whenIsTypedParameter(parameter, tp -> Stream.of(TypeCheckExpression.synthetic(parameterName, tp.type)));
        }

        return result;
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

    private <T> T whenIsTypedParameter(Parameter parameter, Function<TypedParameterTouchPoint, T> fn) {
        if (parameter instanceof SimpleParameter) {
            SimpleParameter simpleParam = (SimpleParameter) parameter;
            return fn.apply(new TypedParameterTouchPoint(scope.resolveType(simpleParam.type).orElse(Scope.TYPE_ANY)));
        }

        return null;
    }
}
