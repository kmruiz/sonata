/*
 * Copyright (c) 2020 Kevin Mas Ruiz
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package io.sonata.lang.analyzer.requires;

import io.sonata.lang.analyzer.Processor;
import io.sonata.lang.analyzer.ProcessorIterator;
import io.sonata.lang.analyzer.ProcessorWrapper;
import io.sonata.lang.analyzer.typeSystem.Scope;
import io.sonata.lang.log.CompilerLog;
import io.sonata.lang.parser.ast.Node;
import io.sonata.lang.parser.ast.RequiresResolver;
import io.sonata.lang.parser.ast.ScriptNode;
import io.sonata.lang.parser.ast.classes.contracts.Contract;
import io.sonata.lang.parser.ast.classes.entities.EntityClass;
import io.sonata.lang.parser.ast.classes.values.ValueClass;
import io.sonata.lang.parser.ast.exp.*;
import io.sonata.lang.parser.ast.let.LetConstant;
import io.sonata.lang.parser.ast.let.LetFunction;
import io.sonata.lang.parser.ast.requires.RequiresNode;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class InlineRequiredModuleProcessor implements ProcessorIterator {
    private final CompilerLog log;
    private final RequiresResolver requiresResolver;

    private InlineRequiredModuleProcessor(CompilerLog log, RequiresResolver requiresResolver) {
        this.log = log;
        this.requiresResolver = requiresResolver;
    }

    public static Processor processorInstance(Scope scope, CompilerLog log, RequiresResolver requiresResolver) {
        return new ProcessorWrapper(scope, "INLINE REQUIRES",
                new InlineRequiredModuleProcessor(log, requiresResolver)
        );
    }

    @Override
    public Node apply(Processor parent, Scope scope, ScriptNode node, List<Node> body) {
        List<Node> nodes = node.nodes.stream().flatMap(element -> {
            if (element instanceof RequiresNode) {
                return recursivelyLoadIfNeeded((RequiresNode) element);
            } else {
                return Stream.of(element);
            }
        }).collect(Collectors.toList());

        return new ScriptNode(node.log, nodes, node.currentNode);
    }

    @Override
    public Expression apply(Processor parent, Scope scope, FunctionCall node, Expression receiver, List<Expression> arguments) {
        return node;
    }

    @Override
    public Expression apply(Processor parent, Scope scope, MethodReference node, Expression receiver) {
        return node;
    }

    @Override
    public Node apply(Processor parent, Scope scope, EntityClass node, List<Node> body) {
        return node;
    }

    @Override
    public Node apply(Processor parent, Scope scope, ValueClass node, List<Node> body) {
        return node;
    }

    @Override
    public Node apply(Processor parent, Scope scope, Contract node, List<Node> body) {
        return node;
    }

    @Override
    public Expression apply(Processor parent, Scope scope, ArrayAccess node, Expression receiver) {
        return node;
    }

    @Override
    public Expression apply(Processor parent, Scope scope, Atom node) {
        return node;
    }

    @Override
    public Expression apply(Processor parent, Scope scope, LiteralArray node, List<Expression> contents) {
        return node;
    }

    @Override
    public Expression apply(Processor parent, Scope scope, PriorityExpression node, Expression content) {
        return node;
    }

    @Override
    public Expression apply(Processor parent, Scope scope, Record node, Map<Atom, Expression> values) {
        return node;
    }

    @Override
    public Expression apply(Processor parent, Scope scope, SimpleExpression node, Expression left, Expression right) {
        return node;
    }

    @Override
    public Expression apply(Processor parent, Scope scope, TypeCheckExpression node) {
        return node;
    }

    @Override
    public Expression apply(Processor parent, Scope scope, ValueClassEquality node, Expression left, Expression right) {
        return node;
    }

    @Override
    public Node apply(Processor parent, Scope scope, RequiresNode node) {
        return node;
    }

    @Override
    public Expression apply(Processor parent, Scope scope, TailExtraction node, Expression receiver) {
        return node;
    }

    @Override
    public Expression apply(Processor parent, Scope scope, BlockExpression node, List<Expression> body) {
        return node;
    }

    @Override
    public Node apply(Processor parent, Scope scope, LetConstant node) {
        return node;
    }

    @Override
    public Node apply(Processor parent, Scope scope, LetFunction node) {
        return node;
    }

    @Override
    public Expression apply(Processor parent, Scope scope, Lambda node, Expression body) {
        return node;
    }

    @Override
    public Expression apply(Processor parent, Scope scope, IfElse node, Expression condition, Expression whenTrue, Expression whenFalse) {
        return node;
    }

    @Override
    public Expression apply(Processor parent, Scope scope, Continuation node, Expression body) {
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
}
