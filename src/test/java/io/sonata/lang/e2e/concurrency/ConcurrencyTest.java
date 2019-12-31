package io.sonata.lang.e2e.concurrency;

import io.sonata.lang.e2e.NodeDockerTest;
import org.junit.jupiter.api.Test;

public class ConcurrencyTest extends NodeDockerTest {
    @Test
    public void shouldRunBothEntitiesInParallel() throws Exception {
        assertResourceScriptOutputs("doing things asynchronously\n0> ping\n0> pong\n1> ping\n1> pong\nping end\npong end", "concurrency/ping-pong");
    }
}
