package io.sonata.lang.e2e.let.fn;

import io.sonata.lang.e2e.E2ETest;
import org.junit.jupiter.api.Test;

public class FunctionDefinitionTest extends E2ETest {
    @Test
    public void functionDefinitionWithoutParameters() throws Exception {
        assertResourceScriptOutputs("Hello World!", "let/fn/function-definition-without-parameters");
    }

    @Test
    public void functionDefinitionWithSingleParameter() throws Exception {
        assertResourceScriptOutputs("Bye World!", "let/fn/function-definition-with-bye-world-parameter");
    }

    @Test
    public void functionDefinitionWithSingleParameterAndConstantOverload() throws Exception {
        assertResourceScriptOutputs("Hey!\nNope!", "let/fn/function-definition-with-single-overloaded-parameter");
    }

    @Test
    public void recursiveFunctionDefinitionWithArrays() throws Exception {
        assertResourceScriptOutputs("55", "let/fn/recursive-function-definition-with-arrays");
    }
}
