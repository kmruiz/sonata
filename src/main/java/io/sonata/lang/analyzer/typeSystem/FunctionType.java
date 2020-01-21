/*
 * Copyright (c) 2020 Kevin Mas Ruiz
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package io.sonata.lang.analyzer.typeSystem;

import io.sonata.lang.parser.ast.let.fn.SimpleParameter;
import io.sonata.lang.source.SourcePosition;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public final class FunctionType implements Type {
    public final SourcePosition definition;
    public final String name;
    public final Type returnType;
    public final List<Type> parameters;

    public FunctionType(SourcePosition definition, String name, Type returnType, List<Type> parameters) {
        this.definition = definition;
        this.name = name;
        this.returnType = returnType;
        this.parameters = parameters;
    }

    @Override
    public SourcePosition definition() {
        return definition;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public boolean canBeReassigned() {
        return false;
    }

    @Override
    public boolean isEntity() {
        return false;
    }

    @Override
    public boolean isValue() {
        return false;
    }

    public List<SimpleParameter> namedParameters() {
        AtomicInteger counter = new AtomicInteger(0);
        char base = 'a';

        return parameters.stream().map(e -> {
            String name = String.valueOf((char) (base + counter.getAndIncrement()));
            return new SimpleParameter(e.definition(), name, null, SimpleParameter.State.END);
        }).collect(Collectors.toList());
    }
}
