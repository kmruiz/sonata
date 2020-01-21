/*
 * Copyright (c) 2020 Kevin Mas Ruiz
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package io.sonata.lang.parser.ast.type;

import io.sonata.lang.source.SourcePosition;
import io.sonata.lang.tokenizer.token.IdentifierToken;
import io.sonata.lang.tokenizer.token.OperatorToken;
import io.sonata.lang.tokenizer.token.SeparatorToken;
import io.sonata.lang.tokenizer.token.Token;

public class EmptyASTTypeRepresentation implements ASTTypeRepresentation {
    private static final EmptyASTTypeRepresentation INSTANCE = new EmptyASTTypeRepresentation();

    public static EmptyASTTypeRepresentation instance() {
        return INSTANCE;
    }

    @Override
    public ASTTypeRepresentation consume(Token token) {
        if (token instanceof IdentifierToken) {
            return BasicASTTypeRepresentation.named(token.sourcePosition(), token.representation());
        }

        if (token instanceof SeparatorToken && token.representation().equals("(")) {
            return PartialFunctionASTTypeRepresentation.inParameterList(token.sourcePosition());
        }

        if (token instanceof OperatorToken && token.representation().equals("->")) {
            return PartialFunctionASTTypeRepresentation.withoutParameters(token.sourcePosition());
        }

        return null;
    }

    @Override
    public String representation() {
        return "<empty type>";
    }

    @Override
    public SourcePosition definition() {
        return null;
    }
}