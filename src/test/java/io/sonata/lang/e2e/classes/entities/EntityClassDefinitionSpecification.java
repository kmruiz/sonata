package io.sonata.lang.e2e.classes.entities;

import io.sonata.lang.e2e.Specification;
import org.junit.jupiter.api.Test;

public class EntityClassDefinitionSpecification extends Specification {
    @Test
    public void definitionOfBasicEntityClass() throws Exception {
        assertResourceScriptOutputs("{ class: 'person', id: 'id', name: 'John' }", "classes/entities/entity-class");
    }
}
