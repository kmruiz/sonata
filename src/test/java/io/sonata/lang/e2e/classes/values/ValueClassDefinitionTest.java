/*
 * Copyright (c) 2020 Kevin Mas Ruiz
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package io.sonata.lang.e2e.classes.values;

import io.sonata.lang.e2e.E2ETest;
import org.junit.jupiter.api.Test;

public class ValueClassDefinitionTest extends E2ETest {
    @Test
    public void definitionOfBasicValueClass() throws Exception {
        assertResourceScriptOutputs("{\"class\":\"price\",\"amount\":42,\"currency\":\"EUR\"}", "classes/values/value-class-price");
    }

    @Test
    public void definitionOfBasicValueClassWithAMethod() throws Exception {
        assertResourceScriptOutputs("42 EUR", "classes/values/value-class-price-with-method");
    }

    @Test
    public void definitionOfBasicValueClassWithAnOverloadedMethod() throws Exception {
        assertResourceScriptOutputs("Try again\nFound it!", "classes/values/value-class-price-with-overloaded-method");
    }

    @Test
    public void definitionOfBasicValueClassWithAMethodWithParameters() throws Exception {
        assertResourceScriptOutputs("John: Hello Bob", "classes/values/value-class-method-with-parameters");
    }

    @Test
    public void destructuringValueClassInLetFunction() throws Exception {
        assertResourceScriptOutputs("42E\n$42", "classes/values/value-class-destructuring");
    }

    @Test
    public void valueClassesShouldCompareTheirFields() throws Exception {
        assertResourceScriptOutputs("true\nfalse\nfalse\ntrue", "classes/values/value-class-equality");
    }
}
