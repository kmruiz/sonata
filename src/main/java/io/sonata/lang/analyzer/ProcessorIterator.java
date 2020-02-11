/*
 * Copyright (c) 2020 Kevin Mas Ruiz
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package io.sonata.lang.analyzer;

import io.sonata.lang.analyzer.typeSystem.Scope;
import io.sonata.lang.parser.ast.Node;
import io.sonata.lang.parser.ast.ScriptNode;
import io.sonata.lang.parser.ast.classes.contracts.Contract;
import io.sonata.lang.parser.ast.classes.entities.EntityClass;
import io.sonata.lang.parser.ast.classes.values.ValueClass;
import io.sonata.lang.parser.ast.exp.*;
import io.sonata.lang.parser.ast.let.LetConstant;
import io.sonata.lang.parser.ast.let.LetFunction;
import io.sonata.lang.parser.ast.requires.RequiresNode;

import java.util.List;
import java.util.Map;

public interface ProcessorIterator {
    Node apply(Processor parent, Scope scope, ScriptNode node, List<Node> body);
    Expression apply(Processor parent, Scope scope, FunctionCall node, Expression receiver, List<Expression> arguments);
    Expression apply(Processor parent, Scope scope, MethodReference node, Expression receiver);
    Node apply(Processor parent, Scope scope, EntityClass node, List<Node> body);
    Node apply(Processor parent, Scope scope, ValueClass node, List<Node> body);
    Node apply(Processor parent, Scope scope, Contract node, List<Node> body);
    Expression apply(Processor parent, Scope scope, ArrayAccess node, Expression receiver);
    Expression apply(Processor parent, Scope scope, Atom node);
    Expression apply(Processor parent, Scope scope, LiteralArray node, List<Expression> contents);
    Expression apply(Processor parent, Scope scope, PriorityExpression node, Expression content);
    Expression apply(Processor parent, Scope scope, Record node, Map<Atom, Expression> values);
    Expression apply(Processor parent, Scope scope, SimpleExpression node, Expression left, Expression right);
    Expression apply(Processor parent, Scope scope, TypeCheckExpression node);
    Expression apply(Processor parent, Scope scope, ValueClassEquality node, Expression left, Expression right);
    Node apply(Processor parent, Scope scope, RequiresNode node);
    Expression apply(Processor parent, Scope scope, TailExtraction node, Expression receiver);
    Expression apply(Processor parent, Scope scope, BlockExpression node, List<Expression> body);
    Node apply(Processor parent, Scope scope, LetConstant node, Expression body);
    Node apply(Processor parent, Scope scope, LetFunction node, Expression body);
    Expression apply(Processor parent, Scope scope, Lambda node, Expression body);
    Expression apply(Processor parent, Scope scope, IfElse node, Expression condition, Expression whenTrue, Expression whenFalse);
    Expression apply(Processor parent, Scope scope, Continuation node, Expression body);
}
