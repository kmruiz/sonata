/*
 * Copyright (c) 2020 Kevin Mas Ruiz
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package io.sonata.lang.e2e.validation;

import io.sonata.lang.e2e.CompilerTest;
import org.junit.jupiter.api.Test;

public class ImmutabilityTest extends CompilerTest {
    @Test
    public void aLetConstantCanNotChangeItsContents() {
        assertSyntaxError("Let constants can not be changed.", "validation/immutability/let-constant");
    }

    @Test
    public void aLetFunctionCanNotChangeItsContents() {
        assertSyntaxError("Let constants can not be changed.", "validation/immutability/let-function");
    }

    @Test
    public void aValueObjectCanNotChangeItsContentsFromOwnMethods() {
        assertSyntaxError("Value objects, built using a value class, are immutable.", "validation/immutability/value-class");
    }

    @Test
    public void aValueObjectCanNotChangeItsContentsFromOutside() {
        assertSyntaxError("Value objects, built using a value class, are immutable.", "validation/immutability/value-class-external");
    }
}
