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
}
