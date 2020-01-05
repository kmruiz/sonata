/*
 * Copyright (c) 2020 Kevin Mas Ruiz
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package io.sonata.lang.tokenizer.token;

import io.sonata.lang.source.SourceCharacter;
import io.sonata.lang.source.SourcePosition;

import java.util.Optional;

public class StringToken implements Token {
    public static boolean isStringStart(char c) {
        return c == '\'';
    }

    public final String value;
    public final boolean escaping;
    public final boolean done;
    public final SourcePosition sourcePosition;

    public StringToken(SourcePosition sourcePosition, String value, boolean escaping, boolean done) {
        this.sourcePosition = sourcePosition;
        this.value = value;
        this.escaping = escaping;
        this.done = done;
    }

    @Override
    public String representation() {
        return value;
    }

    @Override
    public Optional<Token> nextToken(SourceCharacter character) {
        if (done) {
            return Optional.empty();

        }
        if (escaping) {
            return Optional.of(new StringToken(sourcePosition, value + character.character, false, false));
        }

        if (character.character == '\\') {
            return Optional.of(new StringToken(sourcePosition, value + character.character, true, false));
        }

        if (character.character == '\'') {
            return Optional.of(new StringToken(sourcePosition, value + character.character, false, true));
        }

        return Optional.of(new StringToken(sourcePosition, value + character.character, false, false));
    }

    @Override
    public SourcePosition sourcePosition() {
        return sourcePosition;
    }
}
