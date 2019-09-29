package io.sonata.lang.e2e.classes.entities;

import io.sonata.lang.e2e.E2ETest;
import org.junit.jupiter.api.Test;

public class EntityClassDefinitionTest extends E2ETest {
    @Test
    public void definitionOfBasicEntityClass() throws Exception {
        assertResourceScriptOutputs("{ class: 'person', id: 'id', name: 'John' }", "classes/entities/entity-class");
    }
}
