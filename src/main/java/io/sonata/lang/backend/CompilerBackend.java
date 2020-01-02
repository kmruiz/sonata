package io.sonata.lang.backend;

import io.sonata.lang.parser.ast.ScriptNode;

import java.io.IOException;

public interface CompilerBackend {
    void compile(ScriptNode node) throws IOException;
}
