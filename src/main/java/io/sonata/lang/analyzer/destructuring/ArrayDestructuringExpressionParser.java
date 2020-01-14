/*
 * Copyright (c) 2020 Kevin Mas Ruiz
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package io.sonata.lang.analyzer.destructuring;

import io.sonata.lang.parser.ast.Node;
import io.sonata.lang.parser.ast.exp.*;
import io.sonata.lang.parser.ast.let.LetConstant;
import io.sonata.lang.parser.ast.let.fn.ExpressionParameter;
import io.sonata.lang.parser.ast.let.fn.Parameter;
import io.sonata.lang.parser.ast.let.fn.SimpleParameter;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Stream;

public final class ArrayDestructuringExpressionParser implements DestructuringExpressionParser {
    private final class ArrayParameterTouchPoint {
        public final String arrayName;
        public final LiteralArray literalArray;

        private ArrayParameterTouchPoint(String arrayName, LiteralArray literalArray) {
            this.arrayName = arrayName;
            this.literalArray = literalArray;
        }
    }

    @Override
    public Stream<Node> createDestructuringExpression(String parameterName, Parameter parameter) {
        AtomicInteger argIdx = new AtomicInteger(0);
        return whenIsArrayParameter(parameterName, parameter, tp -> tp.literalArray.expressions.stream().map(arg -> {
            int idx = argIdx.getAndIncrement();
            String value = (arg instanceof Atom && ((Atom) arg).type == Atom.Type.IDENTIFIER) ? arg.representation() : identifierFor(idx);
            if (arg instanceof TailExtraction) {
                return new LetConstant(arg.definition(), ((TailExtraction) arg).expression.representation(), null, new TailExtraction(new Atom(arg.definition(), tp.arrayName), idx));
            } else {
                return new LetConstant(arg.definition(), value, null, new ArrayAccess(new Atom(arg.definition(), tp.arrayName), String.valueOf(idx)));
            }
        }));
    }

    @Override
    public Parameter normalizeParameter(String parameterName, Parameter parameter) {
        AtomicInteger argIdx = new AtomicInteger(0);
        Parameter newParam = whenIsArrayParameter(parameterName, parameter, tp -> new SimpleParameter(tp.literalArray.definition(), identifierFor(argIdx.getAndIncrement()), null, SimpleParameter.State.END));
        if (newParam != null) {
            return newParam;
        }

        return parameter;
    }

    @Override
    public Stream<Expression> generateGuardCondition(String parameterName, Parameter parameter) {
        AtomicInteger argIdx = new AtomicInteger(0);
        return whenIsArrayParameter(parameterName, parameter, tp -> tp.literalArray.expressions.stream().map(arg -> {
            int idx = argIdx.getAndIncrement();
            String value = (arg instanceof Atom && ((Atom) arg).type == Atom.Type.IDENTIFIER) ? arg.representation() : identifierFor(idx);

            if (arg instanceof Atom) {
                Atom atom = ((Atom) arg);
                if (atom.type != Atom.Type.IDENTIFIER) {
                    return new SimpleExpression(new Atom(arg.definition(), value), "===", arg);
                } else {
                    return new Atom(arg.definition(), value);
                }
            } else if (arg instanceof TailExtraction) {
                return new SimpleExpression(new MethodReference(new Atom(arg.definition(), parameterName), "length"), ">=", new Atom(arg.definition(), String.valueOf(tp.literalArray.expressions.size())));
            } else if (arg instanceof Expression) {
                return arg;
            }

            return null;
        }));
    }

    private <T> T whenIsArrayParameter(String parameterName, Parameter parameter, Function<ArrayParameterTouchPoint, T> fn) {
        if (parameter instanceof ExpressionParameter) {
            if (((ExpressionParameter) parameter).expression instanceof LiteralArray) {
                LiteralArray literal = ((LiteralArray) ((ExpressionParameter) parameter).expression);
                return fn.apply(new ArrayParameterTouchPoint(parameterName, literal));
            }
        }

        return null;
    }

    private static String identifierFor(int id) {
        return String.valueOf((char) (id + 'a'));
    }
}
