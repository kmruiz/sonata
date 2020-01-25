/*
 * Copyright (c) 2020 Kevin Mas Ruiz
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package io.sonata.lang.parser.ast.let;

import io.sonata.lang.parser.ast.Scoped;
import io.sonata.lang.parser.ast.exp.Expression;
import io.sonata.lang.parser.ast.let.fn.Parameter;
import io.sonata.lang.parser.ast.type.ASTTypeRepresentation;
import io.sonata.lang.parser.ast.type.EmptyASTTypeRepresentation;
import io.sonata.lang.source.SourcePosition;
import io.sonata.lang.tokenizer.token.Token;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class LetFunction implements Expression, Scoped {
    public final String letId;
    public final SourcePosition definition;
    public final String letName;
    public final List<Parameter> parameters;
    public final ASTTypeRepresentation returnType;
    public final Expression body;
    public final boolean isAsync;

    public LetFunction(String letId, SourcePosition definition, String letName, List<Parameter> parameters, ASTTypeRepresentation returnType, Expression body, boolean isAsync) {
        this.letId = letId;
        this.definition = definition;
        this.letName = letName;
        this.parameters = parameters;
        this.returnType = returnType;
        this.body = body;
        this.isAsync = isAsync;
    }

    public LetFunction(SourcePosition definition, String letName, List<Parameter> parameters, ASTTypeRepresentation returnType, Expression body, boolean isAsync) {
        this(UUID.randomUUID().toString(), definition, letName, parameters, returnType, body, isAsync);
    }

    @Override
    public String representation() {
        return "let " + letName + "(" + parameters.stream().map(Parameter::representation).collect(Collectors.joining(", ")) + "): " + returnType.representation() + " = " + body.representation();
    }

    @Override
    public Expression consume(Token token) {
        return null;
    }

    @Override
    public ASTTypeRepresentation type() {
        if (returnType == EmptyASTTypeRepresentation.instance()) {
            return body.type();
        }

        return returnType;
    }

    @Override
    public SourcePosition definition() {
        return definition;
    }

    @Override
    public String scopeId() {
        return "<" + letId + "> let " + letName;
    }
}
