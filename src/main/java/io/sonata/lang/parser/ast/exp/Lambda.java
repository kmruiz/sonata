/*
 * Copyright (c) 2020 Kevin Mas Ruiz
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package io.sonata.lang.parser.ast.exp;

import io.sonata.lang.parser.ast.Node;
import io.sonata.lang.parser.ast.Scoped;
import io.sonata.lang.parser.ast.let.fn.SimpleParameter;
import io.sonata.lang.parser.ast.type.ASTTypeRepresentation;
import io.sonata.lang.parser.ast.type.FunctionASTTypeRepresentation;
import io.sonata.lang.source.SourcePosition;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class Lambda extends ComposedExpression implements Scoped {
    public final String lambdaId;
    public final SourcePosition definition;
    public final List<SimpleParameter> parameters;
    public final Expression body;
    public final boolean isAsync;

    public Lambda(String lambdaId, SourcePosition definition, List<SimpleParameter> parameters, Expression body, boolean isAsync) {
        this.lambdaId = lambdaId;
        this.definition = definition;
        this.parameters = parameters;
        this.body = body;
        this.isAsync = isAsync;
    }

    public Lambda(SourcePosition definition, List<SimpleParameter> parameters, Expression body) {
        this(UUID.randomUUID().toString(), definition, parameters, body, false);
    }

    public static Lambda synthetic(SourcePosition definition, List<SimpleParameter> parameters, Expression body) {
        return new Lambda(definition, parameters, body);
    }

    @Override
    public String representation() {
        return "(" + parameters.stream().map(Node::representation).collect(Collectors.joining(",")) + ") => " + body.representation();
    }

    @Override
    public ASTTypeRepresentation type() {
        return new FunctionASTTypeRepresentation(definition, parameters.stream().map(e -> e.astTypeRepresentation).collect(Collectors.toList()), body.type());
    }

    @Override
    public SourcePosition definition() {
        return definition;
    }

    @Override
    public String scopeId() {
        return lambdaId;
    }
}
