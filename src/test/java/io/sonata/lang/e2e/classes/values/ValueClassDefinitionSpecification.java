package io.sonata.lang.e2e.classes.values;

import io.sonata.lang.e2e.Specification;
import org.junit.jupiter.api.Test;

public class ValueClassDefinitionSpecification extends Specification {
    @Test
    public void definitionOfBasicValueClass() throws Exception {
        assertResourceScriptOutputs("{ class: 'price', amount: 42, currency: 'EUR' }", "classes/values/value-class-price");
    }

    @Test
    public void definitionOfBasicValueClassWithAMethod() throws Exception {
        assertResourceScriptOutputs("42 EUR", "classes/values/value-class-price-with-method");
    }
}
