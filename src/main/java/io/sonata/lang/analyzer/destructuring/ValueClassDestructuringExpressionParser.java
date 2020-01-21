/*
 * Copyright (c) 2020 Kevin Mas Ruiz
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package io.sonata.lang.analyzer.destructuring;

import io.sonata.lang.analyzer.symbols.SymbolResolver;
import io.sonata.lang.javaext.Objects;
import io.sonata.lang.parser.ast.Node;
import io.sonata.lang.parser.ast.classes.values.ValueClass;
import io.sonata.lang.parser.ast.exp.Atom;
import io.sonata.lang.parser.ast.exp.Expression;
import io.sonata.lang.parser.ast.exp.FunctionCall;
import io.sonata.lang.parser.ast.exp.SimpleExpression;
import io.sonata.lang.parser.ast.let.LetConstant;
import io.sonata.lang.parser.ast.let.fn.ExpressionParameter;
import io.sonata.lang.parser.ast.let.fn.Parameter;
import io.sonata.lang.parser.ast.let.fn.SimpleParameter;
import io.sonata.lang.parser.ast.type.BasicASTTypeRepresentation;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Stream;

public final class ValueClassDestructuringExpressionParser implements DestructuringExpressionParser {
    private final SymbolResolver resolver;

    public ValueClassDestructuringExpressionParser(SymbolResolver resolver) {
        this.resolver = resolver;
    }

    private final class ValueClassParameterTouchPoint {
        public final FunctionCall functionCall;
        public final ValueClass valueClass;

        private ValueClassParameterTouchPoint(FunctionCall functionCall, ValueClass valueClass) {
            this.functionCall = functionCall;
            this.valueClass = valueClass;
        }

    }

    public Stream<Node> createDestructuringExpression(String parameterName, Parameter parameter) {
        AtomicInteger argIdx = new AtomicInteger(-1);
        return whenIsValueClassParameter(parameter, tp -> tp.functionCall.arguments.stream().map(arg -> {
            int field = argIdx.incrementAndGet();
            String fieldName = tp.valueClass.definedFields.get(field).name();

            return new LetConstant(arg.definition(), fieldName, null, new SimpleExpression(new Atom(arg.definition(), Objects.requireNonNullElse(parameterName, tp.valueClass.name)), ".", new Atom(arg.definition(), fieldName)));
        }));
    }

    public Parameter normalizeParameter(String parameterName, Parameter parameter) {
        Parameter newParam = whenIsValueClassParameter(parameter, tp -> new SimpleParameter(tp.valueClass.definition, tp.valueClass.name, new BasicASTTypeRepresentation(tp.valueClass.definition(), tp.valueClass.name), SimpleParameter.State.END));
        if (newParam != null) {
            return newParam;
        }

        return parameter;
    }

    public Stream<Expression> generateGuardCondition(String parameterName, Parameter parameter) {
        AtomicInteger argIdx = new AtomicInteger(-1);
        return whenIsValueClassParameter(parameter, tp -> tp.functionCall.arguments.stream().map(arg -> {
            int field = argIdx.incrementAndGet();

            if (arg instanceof Atom) {
                Atom atom = ((Atom) arg);
                if (atom.type != Atom.Type.IDENTIFIER) {
                    return new SimpleExpression(new Atom(atom.definition(), tp.valueClass.definedFields.get(field).name()), "===", arg);
                }
            } else if (arg instanceof Expression) {
                return arg;
            }

            return null;
        }));
    }

    private <T> T whenIsValueClassParameter(Parameter parameter, Function<ValueClassParameterTouchPoint, T> fn) {
        if (parameter instanceof ExpressionParameter) {
            if (((ExpressionParameter) parameter).expression instanceof FunctionCall) {
                Expression receiver = ((FunctionCall) ((ExpressionParameter) parameter).expression).receiver;
                if (receiver instanceof Atom) {
                    String receiverName = ((Atom) receiver).value;
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
