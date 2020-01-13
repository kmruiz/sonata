/*
 * Copyright (c) 2020 Kevin Mas Ruiz
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package io.sonata.lang.parser.ast;

import io.sonata.lang.parser.ast.classes.entities.PartialEntityClass;
import io.sonata.lang.parser.ast.classes.values.PartialValueClass;
import io.sonata.lang.parser.ast.exp.Atom;
import io.sonata.lang.parser.ast.exp.EmptyExpression;
import io.sonata.lang.parser.ast.exp.ifelse.PartialIf;
import io.sonata.lang.parser.ast.let.PartialLet;
import io.sonata.lang.parser.ast.requires.PartialRequiresNode;
import io.sonata.lang.source.SourcePosition;
import io.sonata.lang.tokenizer.token.IdentifierToken;
import io.sonata.lang.tokenizer.token.OperatorToken;
import io.sonata.lang.tokenizer.token.SeparatorToken;
import io.sonata.lang.tokenizer.token.Token;

public class RootNode implements Node {
    private static final RootNode INSTANCE = new RootNode();

    public static RootNode instance() {
        return INSTANCE;
    }

    @Override
    public String representation() {
        return "<root>";
    }

    @Override
    public Node consume(Token token) {
        if (token instanceof IdentifierToken) {
            switch (token.representation()) {
                case "let":
                    return PartialLet.initial(token.sourcePosition());
                case "requires":
                    return PartialRequiresNode.initial(token.sourcePosition());
                case "entity":
                    return PartialEntityClass.initial(token.sourcePosition());
                case "value":
                    return PartialValueClass.initial(token.sourcePosition());
                case "if":
                    return PartialIf.initial(token.sourcePosition());
            }

            return new Atom(token.sourcePosition(), token.representation());
        }

        if (token instanceof SeparatorToken && token.representation().equals("\n")) {
            return this;
        }

        if (token instanceof OperatorToken && token.representation().equals("#")) {
            return new CommentNode(token.sourcePosition());
        }
        return EmptyExpression.instance().consume(token);
    }

    @Override
    public SourcePosition definition() {
        return null;
    }
}
