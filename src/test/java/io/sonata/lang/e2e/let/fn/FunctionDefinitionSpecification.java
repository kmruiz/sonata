package io.sonata.lang.e2e.let.fn;

import io.sonata.lang.e2e.Specification;
import org.junit.jupiter.api.Test;

public class FunctionDefinitionSpecification extends Specification {
    @Test
    public void functionDefinitionWithoutParameters() throws Exception {
        assertResourceScriptOutputs("Hello World!", "let/fn/function-definition-without-parameters");
    }
}
