/*
 * Copyright (c) 2020 Kevin Mas Ruiz
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package io.sonata.lang.parser.ast.type;

import io.sonata.lang.source.SourcePosition;
import io.sonata.lang.tokenizer.token.SeparatorToken;
import io.sonata.lang.tokenizer.token.Token;

import java.util.Collections;
import java.util.List;

import static io.sonata.lang.javaext.Lists.append;

public class PartialGenericASTType implements ASTType {
    public final ASTType base;
    public final List<ASTType> parameters;
    public final ASTType current;

    private PartialGenericASTType(ASTType base, List<ASTType> parameters, ASTType current) {
        this.base = base;
        this.parameters = parameters;
        this.current = current;
    }

    public static PartialGenericASTType on(ASTType base) {
        return new PartialGenericASTType(base, Collections.emptyList(), EmptyASTType.instance());
    }

    @Override
    public ASTType consume(Token token) {
        ASTType next = current.consume(token);
        if (next == null) {
            if (token instanceof SeparatorToken) {
                SeparatorToken sep = (SeparatorToken) token;
                if (sep.separator.equals(",")) {
                    return new PartialGenericASTType(base, append(parameters, current), EmptyASTType.instance());
                }

                if (sep.separator.equals("]")) {
                    if (parameters.size() == 0 && current instanceof EmptyASTType) {
                        return new ArrayASTType(base);
                    }

                    return new GenericASTType(base, append(parameters, current));
                }
            }
        }

        return new PartialGenericASTType(base, parameters, next);
    }

    @Override
    public String representation() {
        return null;
    }

    @Override
    public SourcePosition definition() {
        return base.definition();
    }
}
