/*
 * Copyright (c) 2020 Kevin Mas Ruiz
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package io.sonata.lang.analyzer.requires;

import io.sonata.lang.analyzer.Processor;
import io.sonata.lang.log.CompilerLog;
import io.sonata.lang.parser.ast.Node;
import io.sonata.lang.parser.ast.RequiresResolver;
import io.sonata.lang.parser.ast.ScriptNode;
import io.sonata.lang.parser.ast.requires.RequiresNode;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class InlineRequiredModuleProcessor implements Processor {
    private final CompilerLog log;
    private final RequiresResolver requiresResolver;

    public InlineRequiredModuleProcessor(CompilerLog log, RequiresResolver requiresResolver) {
        this.log = log;
        this.requiresResolver = requiresResolver;
    }

    @Override
    public Node apply(Node node) {
        if (node instanceof ScriptNode) {
            ScriptNode scriptNode = (ScriptNode) node;
            List<Node> nodes = scriptNode.nodes.stream().flatMap(body -> {
                if (body instanceof RequiresNode) {
                    return recursivelyLoadIfNeeded((RequiresNode) body);
                } else {
                    return Stream.of(body);
                }
            }).collect(Collectors.toList());
            return new ScriptNode(scriptNode.log, nodes, scriptNode.currentNode);
        }

        return node;
    }

    private Stream<Node> recursivelyLoadIfNeeded(RequiresNode requires) {
        final String module = requires.module;
        try {
            return requiresResolver
                    .replaceModule(module)
                    .map(Stream::of)
                    .orElse(Stream.empty())
                    .flatMap(e -> e.nodes.stream())
                    .flatMap(body -> {
                        if (body instanceof RequiresNode) {
                            return recursivelyLoadIfNeeded((RequiresNode) body);
                        } else {
                            return Stream.of(body);
                        }
                    });
        } catch (IOException e) {
            log.compilerError(e);
            return Stream.empty();
        }
    }

    @Override
    public String phase() {
        return "INLINE REQUIRES";
    }
}
