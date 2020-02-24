/*
 * Copyright (c) 2020 Kevin Mas Ruiz
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package io.sonata.lang.parser.ast.classes.contracts;

import io.sonata.lang.exception.ParserException;
import io.sonata.lang.parser.ast.Node;
import io.sonata.lang.source.SourcePosition;
import io.sonata.lang.tokenizer.token.Token;

import java.util.Collections;
import java.util.List;

import static io.sonata.lang.javaext.Lists.append;

public class PartialContractWithExtensions implements Node {
    enum State {
        ON_CONTRACT,
        ON_SEPARATOR
    }

    private final SourcePosition definition;
    private final String name;
    private final State state;
    private final List<String> extensions;

    public PartialContractWithExtensions(SourcePosition definition, String name, State state, List<String> extensions) {
        this.definition = definition;
        this.name = name;
        this.state = state;
        this.extensions = extensions;
    }

    public static PartialContractWithExtensions initial(SourcePosition definition, String name) {
        return new PartialContractWithExtensions(definition, name, State.ON_CONTRACT, Collections.emptyList());
    }

    @Override
    public Node consume(Token token) {
        switch (state) {
            case ON_CONTRACT:
                return new PartialContractWithExtensions(definition, name,State.ON_SEPARATOR, append(extensions, token.representation()));
            case ON_SEPARATOR:
                if (token.representation().equals("{")) {
                    return PartialContractWithBody.initial(definition, name, extensions);
                }

                if (token.representation().equals("\n")) {
                    return new Contract(definition, token.representation(), Collections.emptyList(), extensions);
                }

                return new PartialContractWithExtensions(definition, name, State.ON_CONTRACT, extensions);
        }

        throw new ParserException(this, "Unexpected token " + token.representation());
    }

    @Override
    public String representation() {
        return String.format("contract %s", name);
    }

    @Override
    public SourcePosition definition() {
        return definition;
    }
}
