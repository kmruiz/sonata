package io.sonata.lang.e2e.classes.values;

import io.sonata.lang.e2e.E2ETest;
import org.junit.jupiter.api.Test;

public class ValueClassDefinitionTest extends E2ETest {
    @Test
    public void definitionOfBasicValueClass() throws Exception {
        assertResourceScriptOutputs("{ class: 'price', amount: 42, currency: 'EUR' }", "classes/values/value-class-price");
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
}
