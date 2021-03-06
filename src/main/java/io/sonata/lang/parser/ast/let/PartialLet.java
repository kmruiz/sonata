/*
 * Copyright (c) 2020 Kevin Mas Ruiz
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package io.sonata.lang.parser.ast.let;

import io.sonata.lang.exception.ParserException;
import io.sonata.lang.parser.ast.exp.Expression;
import io.sonata.lang.parser.ast.type.ASTTypeRepresentation;
import io.sonata.lang.source.SourcePosition;
import io.sonata.lang.tokenizer.token.IdentifierToken;
import io.sonata.lang.tokenizer.token.SeparatorToken;
import io.sonata.lang.tokenizer.token.Token;

public class PartialLet implements Expression {
    public final SourcePosition definition;
    public final String letName;

    private PartialLet(SourcePosition definition, String letName) {
        this.definition = definition;
        this.letName = letName;
    }

    public static PartialLet initial(SourcePosition definition) {
        return new PartialLet(definition, null);
    }

    @Override
    public String representation() {
        return "let " + (letName == null ? "?" : letName);
    }

    @Override
    public Expression consume(Token token) {
        if (letName == null) {
            if (token instanceof IdentifierToken) {
                return new PartialLet(definition, ((IdentifierToken) token).value);
            } else if (token instanceof SeparatorToken) {
                SeparatorToken sep = (SeparatorToken) token;

                if (sep.separator.equals("(")) {
                    return PartialLetFunction.anonymous(definition);
                }
            }

            throw new ParserException(this, "Expected an identifier or an opening parenthesis, but got '" + token.representation() + "'");
        } else {
            if (token instanceof SeparatorToken) {
                SeparatorToken sep = (SeparatorToken) token;

                if (sep.separator.equals("(")) {
                    return PartialLetFunction.initial(definition, letName);
                }
            }
        }

        return PartialLetConstant.initial(token.sourcePosition(), letName).consume(token);
    }

    @Override
    public ASTTypeRepresentation type() {
        return null;
    }

    @Override
    public SourcePosition definition() {
        return definition;
    }
}
