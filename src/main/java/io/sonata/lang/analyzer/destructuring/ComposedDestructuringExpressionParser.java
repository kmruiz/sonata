/*
 * Copyright (c) 2020 Kevin Mas Ruiz
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
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
