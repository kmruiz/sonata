package io.sonata.lang.e2e.let.fn;

import io.sonata.lang.e2e.Specification;
import org.junit.jupiter.api.Test;

public class FunctionDefinitionSpecification extends Specification {
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

    @Test
    public void lambdaWithQuestionMark() throws Exception {
        assertResourceScriptOutputs("[ 10, 20, 30, 40, 50 ]", "let/fn/implicit-lambda-with-question-mark");
    }
}
