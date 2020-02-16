/*
 * Copyright (c) 2020 Kevin Mas Ruiz
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package io.sonata.lang.e2e.lambda;

import io.sonata.lang.e2e.E2ETest;
import org.junit.jupiter.api.Test;

public class LambdaTest extends E2ETest {
    @Test
    public void basicUsageOfLambda() throws Exception {
        assertResourceScriptOutputs("Hello", "lambda/no-param");
    }

    @Test
    public void basicUsageOfLambdaWithParams() throws Exception {
        assertResourceScriptOutputs("15", "lambda/with-params");
    }

    @Test
    public void basicUsageOfLambdaWithParamsIsPair() throws Exception {
        assertResourceScriptOutputs("true\nfalse", "lambda/lambda-is-pair");
    }

    @Test
    public void lambdaWithQuestionMark() throws Exception {
        assertResourceScriptOutputs("10,20,30,40,50", "lambda/implicit-lambda-with-question-mark");
    }

    @Test
    public void lambdaWithMultipleQuestionMarks() throws Exception {
        assertResourceScriptOutputs("15", "lambda/implicit-lambda-with-multiple-question-marks");
    }

    @Test
    public void lambdaWithMultipleQuestionMarksAndReference() throws Exception {
        assertResourceScriptOutputs("15", "lambda/implicit-lambda-as-reference");
    }


    @Test
    public void lambdaWithSimpleInferredArguments() throws Exception {
        assertResourceScriptOutputs("HELLO WORLD", "lambda/lambda-with-simple-arguments");
    }
}
