/*
 * Copyright (c) 2020 Kevin Mas Ruiz
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package io.sonata.lang.parser.ast.exp;

import io.sonata.lang.parser.ast.type.ASTTypeRepresentation;
import io.sonata.lang.parser.ast.type.BasicASTTypeRepresentation;
import io.sonata.lang.parser.ast.type.GenericASTTypeRepresentation;
import io.sonata.lang.source.SourcePosition;

public class ArrayAccess extends ComposedExpression implements Expression {
    public final Expression receiver;
    public final Expression index;

    public ArrayAccess(Expression receiver, Expression index) {
        this.receiver = receiver;
        this.index = index;
    }

    @Override
    public String representation() {
        return receiver.representation() + "[" + index + "]";
    }

    @Override
    public ASTTypeRepresentation type() {
        final ASTTypeRepresentation type = receiver.type();
        if (type instanceof GenericASTTypeRepresentation) {
            return ((GenericASTTypeRepresentation) type).parameters.get(0);
        }

        return new BasicASTTypeRepresentation(receiver.definition(), "any");
    }

    @Override
    public SourcePosition definition() {
        return receiver.definition();
    }
}
