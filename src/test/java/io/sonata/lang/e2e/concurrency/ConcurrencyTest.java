/*
 * Copyright (c) 2020 Kevin Mas Ruiz
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

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
