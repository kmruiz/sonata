package io.sonata.lang.e2e.lambda;

import io.sonata.lang.e2e.E2ETest;
import org.junit.jupiter.api.Test;

public class LambdaTest extends E2ETest {
    @Test
    public void basicUsageOfLambda() throws Exception {
        assertResourceScriptOutputs("Hello\nHello", "lambda/no-param");
    }
}
