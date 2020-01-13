package io.sonata.lang.e2e;

import org.junit.jupiter.api.Test;

public class BasicStructTest extends E2ETest {
    @Test
    public void commentInScriptRoot() throws Exception {
        assertResourceScriptOutputs("Hello World!", "basic/comments");
    }

    @Test
    public void commentInBlock() throws Exception {
        assertResourceScriptOutputs("Hello World!", "basic/comments-in-block");
    }
}
