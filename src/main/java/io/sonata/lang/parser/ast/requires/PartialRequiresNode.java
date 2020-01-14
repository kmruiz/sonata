/*
 * Copyright (c) 2020 Kevin Mas Ruiz
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package io.sonata.lang.parser.ast.requires;

import io.sonata.lang.exception.ParserException;
import io.sonata.lang.parser.ast.Node;
import io.sonata.lang.source.SourcePosition;
import io.sonata.lang.tokenizer.token.IdentifierToken;
import io.sonata.lang.tokenizer.token.Token;

public class PartialRequiresNode implements Node {
    public static PartialRequiresNode initial(SourcePosition definition) {
        return new PartialRequiresNode(definition, "", State.IN_NAME);
    }

    private PartialRequiresNode(SourcePosition definition, String module, State state) {
        this.definition = definition;
        this.module = module;
        this.state = state;
    }

    enum State {
        IN_SEPARATOR, IN_NAME
    }

    private final SourcePosition definition;
    private final String module;
    private final State state;

    @Override
    public String representation() {
        return "requires " + module;
    }

    @Override
    public Node consume(Token token) {
        switch (state) {
            case IN_NAME:
                if (token instanceof IdentifierToken) {
                    return new PartialRequiresNode(definition, module + token.representation(), State.IN_SEPARATOR);
                }

                throw new ParserException(this, "Expecting identifier, but got '" + token.representation() + "'");
            case IN_SEPARATOR:
                if (token.representation().equals("\n")) {
                    return new RequiresNode(definition, module);
                }

                if (token.representation().equals(".")) {
                    return new PartialRequiresNode(definition, module + ".", State.IN_NAME);
                }

                throw new ParserException(this, "Expecting a new line, to finish the statement, or a dot '.' to continue with the definition, but got '" + token.representation() + "'");

        }

        return null;
    }

    @Override
    public SourcePosition definition() {
        return definition;
    }
}
