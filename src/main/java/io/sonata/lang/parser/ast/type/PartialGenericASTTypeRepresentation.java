/*
 * Copyright (c) 2020 Kevin Mas Ruiz
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

public class PartialGenericASTTypeRepresentation implements ASTTypeRepresentation {
    public final ASTTypeRepresentation base;
    public final List<ASTTypeRepresentation> parameters;
    public final ASTTypeRepresentation current;

    private PartialGenericASTTypeRepresentation(ASTTypeRepresentation base, List<ASTTypeRepresentation> parameters, ASTTypeRepresentation current) {
        this.base = base;
        this.parameters = parameters;
        this.current = current;
    }

    public static PartialGenericASTTypeRepresentation on(ASTTypeRepresentation base) {
        return new PartialGenericASTTypeRepresentation(base, Collections.emptyList(), EmptyASTTypeRepresentation.instance());
    }

    @Override
    public ASTTypeRepresentation consume(Token token) {
        ASTTypeRepresentation next = current.consume(token);
        if (next == null) {
            if (token instanceof SeparatorToken) {
                SeparatorToken sep = (SeparatorToken) token;
                if (sep.separator.equals(",")) {
                    return new PartialGenericASTTypeRepresentation(base, append(parameters, current), EmptyASTTypeRepresentation.instance());
                }

                if (sep.separator.equals("]")) {
                    if (parameters.size() == 0 && current instanceof EmptyASTTypeRepresentation) {
                        return new ArrayASTTypeRepresentation(base);
                    }

                    return new GenericASTTypeRepresentation(base, append(parameters, current));
                }
            }
        }

        return new PartialGenericASTTypeRepresentation(base, parameters, next);
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
