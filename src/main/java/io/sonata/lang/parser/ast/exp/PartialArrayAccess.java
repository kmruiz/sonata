/*
 * Copyright (c) 2020 Kevin Mas Ruiz
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package io.sonata.lang.parser.ast.exp;

import io.sonata.lang.parser.ast.type.ASTType;
import io.sonata.lang.source.SourcePosition;
import io.sonata.lang.tokenizer.token.NumericToken;
import io.sonata.lang.tokenizer.token.SeparatorToken;
import io.sonata.lang.tokenizer.token.Token;

public class PartialArrayAccess implements Expression {
    public final SourcePosition definition;
    public final Expression receiver;
    public final String index;

    private PartialArrayAccess(SourcePosition definition, Expression receiver, String index) {
        this.definition = definition;
        this.receiver = receiver;
        this.index = index;
    }

    public static PartialArrayAccess on(SourcePosition definition, Expression receiver) {
        return new PartialArrayAccess(definition, receiver, null);
    }

    @Override
    public Expression consume(Token token) {
        if (token instanceof NumericToken && index == null) {
            NumericToken idx = (NumericToken) token;
            return new PartialArrayAccess(definition, receiver, idx.value);
        }

        if (token instanceof SeparatorToken && index != null) {
            SeparatorToken sep = (SeparatorToken) token;
            if (sep.separator.equals("]")) {
                return new ArrayAccess(receiver, index);
            }
        }

        return null;
    }

    @Override
    public ASTType type() {
        return null;
    }

    @Override
    public String representation() {
        return null;
    }

    @Override
    public SourcePosition definition() {
        return definition;
    }
}
