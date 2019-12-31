package io.sonata.lang.e2e.concurrency;

import io.sonata.lang.e2e.E2ETest;
import org.junit.jupiter.api.Test;

public class ConcurrencyTest extends E2ETest {
    @Test
    public void shouldRunBothEntitiesInParallel() {
        assertResourceScriptOutputs("Bye World!", "concurrency/ping-pong");
    }
}
