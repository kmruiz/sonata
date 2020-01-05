/*
 * Copyright (c) 2020 Kevin Mas Ruiz
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package io.sonata.lang.source;

public class SourcePosition {
    public final Source source;
    public final int line;
    public final int column;

    private SourcePosition(Source source, int line, int column) {
        this.source = source;
        this.line = line;
        this.column = column;
    }

    public static SourcePosition initial(Source source) {
        return new SourcePosition(source, 1, 0);
    }

    public final SourcePosition next(char byChar) {
        if (byChar == '\n') {
            return new SourcePosition(source, line + 1, 0);
        }
        return new SourcePosition(source, line, column + 1);
    }

    @Override
    public String toString() {
        return String.format("[%s:%d:%d]", source.name, line, column);
    }
}
