/*
 * Copyright (c) 2020 Kevin Mas Ruiz
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package io.sonata.lang.tokenizer.token;

import io.sonata.lang.source.Source;
import io.sonata.lang.source.SourceCharacter;
import io.sonata.lang.source.SourcePosition;

import java.util.Optional;

public class RootToken implements Token {
    private static final RootToken INSTANCE = new RootToken();

    public static RootToken instance() {
        return INSTANCE;
    }

    @Override
    public String representation() {
        return "<root>";
    }

    @Override
    public Optional<Token> nextToken(SourceCharacter character) {
        String initial = String.valueOf(character.character);

        if (IdentifierToken.isIdentifier(character.character)) {
            return Optional.of(new IdentifierToken(character.position, initial));
        }

        if (SeparatorToken.isSeparator(character.character)) {
            return Optional.of(new SeparatorToken(character.position, initial));
        }

        if (OperatorToken.isOperator(character.character)) {
            return Optional.of(new OperatorToken(character.position, initial));
        }

        if (NumericToken.isNumeric(character.character)) {
            return Optional.of(new NumericToken(character.position, initial));
        }

        if (StringToken.isStringStart(character.character)) {
            return Optional.of(new StringToken(character.position, initial, false, false));
        }

        if (CommentToken.isComment(character.character)) {
            return Optional.of(new CommentToken(character.position, ""));
        }

        return Optional.of(this);
    }

    @Override
    public SourcePosition sourcePosition() {
        return SourcePosition.initial(Source.fromLiteral("<RootToken>"));
    }
}
