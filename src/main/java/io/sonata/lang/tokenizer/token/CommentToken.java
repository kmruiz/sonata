/*
 * Copyright (c) 2020 Kevin Mas Ruiz
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package io.sonata.lang.tokenizer.token;

import io.sonata.lang.source.SourceCharacter;
import io.sonata.lang.source.SourcePosition;

import java.util.Optional;

public class CommentToken implements Token {
    public static boolean isComment(char c) {
        return c == ';';
    }

    public final String content;
    public final SourcePosition sourcePosition;

    public CommentToken(SourcePosition sourcePosition, String content) {
        this.sourcePosition = sourcePosition;
        this.content = content;
    }

    @Override
    public String representation() {
        return ";" + content;
    }

    @Override
    public Optional<Token> nextToken(SourceCharacter character) {
        if (character.character == '\n') {
            return Optional.empty();
        } else {
            return Optional.of(new CommentToken(sourcePosition, content + character.character));
        }
    }

    @Override
    public SourcePosition sourcePosition() {
        return sourcePosition;
    }
}
