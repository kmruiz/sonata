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

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class SeparatorToken implements Token {
    private static final Set<Character> SEPARATORS;

    static {
        SEPARATORS = new HashSet<>();
        SEPARATORS.add('(');
        SEPARATORS.add(')');
        SEPARATORS.add('[');
        SEPARATORS.add(']');
        SEPARATORS.add('{');
        SEPARATORS.add('}');
        SEPARATORS.add(':');
        SEPARATORS.add(',');
        SEPARATORS.add(';');
        SEPARATORS.add('.');
        SEPARATORS.add('?');
        SEPARATORS.add('\n');
        SEPARATORS.add('\0');
    }

    public static boolean isSeparator(char c) {
        return SEPARATORS.contains(c);
    }

    public final String separator;
    public final SourcePosition sourcePosition;

    public SeparatorToken(SourcePosition sourcePosition, String separator) {
        this.sourcePosition = sourcePosition;
        this.separator = separator;
    }

    @Override
    public String representation() {
        return separator;
    }

    @Override
    public Optional<Token> nextToken(SourceCharacter character) {
        return Optional.empty();
    }

    @Override
    public SourcePosition sourcePosition() {
        return sourcePosition;
    }
}
