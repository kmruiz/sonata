package io.sonata.lang.e2e.validation;

import io.sonata.lang.e2e.E2ETest;
import org.junit.jupiter.api.Test;

public class PropertyVisibilityTest extends E2ETest {
    @Test
    public void anEntityShouldNotBeAbleToAccessPropertiesOfAnotherEntityEvenIfItsTheSameType() {
        assertSyntaxError("It's forbidden to access properties of another entity instance.", "validation/properties/entity-to-entity");
    }

    @Test
    public void aValueObjectShouldNotBeAbleToAccessEntityProperties() {
        assertSyntaxError("It's forbidden to access properties of another entity instance.", "validation/properties/value-to-entity");
    }
}
