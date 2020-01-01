package io.sonata.lang.e2e.concurrency;

import io.sonata.lang.e2e.NodeDockerTest;
import org.junit.jupiter.api.Test;

public class ConcurrencyTest extends NodeDockerTest {
    @Test
    public void shouldRunBothEntitiesInParallel() throws Exception {
        assertResourceScriptOutputs("doing things asynchronously\n0> ping\n0> pong\n1> ping\n1> pong\nping end\npong end", "concurrency/ping-pong");
    }

    @Test
    public void shouldRunBothEntitiesInParallelWithState() throws Exception {
        assertResourceScriptOutputs("doing things asynchronously\n1> ping\n1> pong\n2> ping\n2> pong", "concurrency/ping-pong-stateful");
    }

    @Test
    public void shouldDoContinuationsItselfUsingPromises() throws Exception {
        assertResourceScriptOutputs("asking for value\nHello World!\nvalue asked", "concurrency/async-ask");
    }
}
