/*
 * Copyright (c) 2020 Kevin Mas Ruiz
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package io.sonata.lang.e2e.let.fn;

import io.sonata.lang.e2e.EndToEndTest;
import org.junit.jupiter.api.Test;

public class FunctionDefinitionTest extends EndToEndTest {
    @Test
    public void functionDefinitionWithoutParameters() throws Exception {
        assertResourceScriptOutputs("Hello World!", "/e2e/let/fn/function-definition-without-parameters.sn");
    }

    @Test
    public void functionDefinitionWithSingleParameter() throws Exception {
        assertResourceScriptOutputs("Bye World!", "/e2e/let/fn/function-definition-with-bye-world-parameter.sn");
    }

    @Test
    public void functionDefinitionWithSingleParameterAndConstantOverload() throws Exception {
        assertResourceScriptOutputs("Hey!\nNope!", "/e2e/let/fn/function-definition-with-single-overloaded-parameter.sn");
    }

    @Test
    public void recursiveFunctionDefinitionWithArrays() throws Exception {
        assertResourceScriptOutputs("55", "/e2e/let/fn/recursive-function-definition-with-arrays.sn");
    }

    @Test
    public void functionWithBlockBody() throws Exception {
        assertResourceScriptOutputs("a\nb", "/e2e/let/fn/function-with-block.sn");
    }

    @Test
    public void functionDefinitionRuntimeDispatchDifferentTypes() throws Exception {
        assertResourceScriptOutputs("Number\nPrice\nString", "/e2e/let/fn/function-overload-multiple-types.sn");
    }
}
