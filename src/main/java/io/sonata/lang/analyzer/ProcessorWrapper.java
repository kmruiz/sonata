/*
 * Copyright (c) 2020 Kevin Mas Ruiz
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package io.sonata.lang.analyzer;

import io.sonata.lang.analyzer.typeSystem.Scope;
import io.sonata.lang.exception.ParserException;
import io.sonata.lang.parser.ast.Node;
import io.sonata.lang.parser.ast.RootNode;
import io.sonata.lang.parser.ast.ScriptNode;
import io.sonata.lang.parser.ast.classes.contracts.Contract;
import io.sonata.lang.parser.ast.classes.entities.EntityClass;
import io.sonata.lang.parser.ast.classes.values.ValueClass;
import io.sonata.lang.parser.ast.exp.*;
import io.sonata.lang.parser.ast.let.LetConstant;
import io.sonata.lang.parser.ast.let.LetFunction;
import io.sonata.lang.parser.ast.requires.RequiresNode;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ProcessorWrapper implements Processor {
    private final Scope root;
    private final String phase;
    private final ProcessorIterator iterator;

    public ProcessorWrapper(Scope root, String phase, ProcessorIterator iterator) {
        this.root = root;
        this.phase = phase;
        this.iterator = iterator;
    }

    @Override
    public Node apply(Node node) {
        return apply(root, node);
    }

    public Node apply(Scope scope, Node node) {
        if (node instanceof ScriptNode) {
            ScriptNode script = (ScriptNode) node;
            Scope scriptScope = scope.diveInIfNeeded(script);

            List<Node> body = script.nodes.stream().map(e -> this.apply(scriptScope, e)).collect(Collectors.toList());

            return iterator.apply(this, scope, script, body);
        }

        if (node instanceof FunctionCall) {
            FunctionCall fc = (FunctionCall) node;
            Scope fcScope = scope.diveInIfNeeded(fc);

            List<Expression> args = fc.arguments.stream().map(e -> this.apply(fcScope, e)).map(e -> (Expression) e).collect(Collectors.toList());
            Expression receiver = (Expression) this.apply(scope, fc.receiver);

            return iterator.apply(this, scope, fc, receiver, args);
        }

        if (node instanceof MethodReference) {
            MethodReference mr = (MethodReference) node;
            Expression receiver = (Expression) this.apply(scope, mr.receiver);
            return iterator.apply(this, scope, mr, receiver);
        }

        if (node instanceof EntityClass) {
            EntityClass ec = (EntityClass) node;
            Scope ecScope = scope.diveInIfNeeded(ec);

            List<Node> body = ec.body.stream().map(e -> this.apply(ecScope, e)).collect(Collectors.toList());
            return iterator.apply(this, scope, ec, body);
        }

        if (node instanceof ValueClass) {
            ValueClass vc = (ValueClass) node;
            Scope vcScope = scope.diveInIfNeeded(vc);

            List<Node> body = vc.body.stream().map(e -> this.apply(vcScope, e)).collect(Collectors.toList());
            return iterator.apply(this, scope, vc, body);
        }

        if (node instanceof Contract) {
            Contract ct = (Contract) node;
            Scope ctScope = scope.diveInIfNeeded(ct);

            List<Node> body = ct.body.stream().map(e -> this.apply(ctScope, e)).collect(Collectors.toList());
            return iterator.apply(this, scope, ct, body);
        }

        if (node instanceof ArrayAccess) {
            ArrayAccess arrayAccess = (ArrayAccess) node;
            Expression receiver = (Expression) this.apply(scope, arrayAccess.receiver);
            return iterator.apply(this, scope, arrayAccess, receiver);
        }

        if (node instanceof Atom) {
            Atom atom = (Atom) node;
            return iterator.apply(this, scope, atom);
        }

        if (node instanceof LiteralArray) {
            LiteralArray array = (LiteralArray) node;
            List<Expression> expr = array.expressions.stream().map(e -> this.apply(scope, e)).map(e -> (Expression) e).collect(Collectors.toList());

            return iterator.apply(this, scope, array, expr);
        }

        if (node instanceof PriorityExpression) {
            PriorityExpression prio = (PriorityExpression) node;
            Expression expr = (Expression) this.apply(scope, prio.expression);

            return iterator.apply(this, scope, prio, expr);
        }

        if (node instanceof Record) {
            Record record = (Record) node;
            Map<Atom, Expression> recordData = record.values.entrySet().stream().map(entry -> new AbstractMap.SimpleImmutableEntry<>(entry.getKey(), (Expression) apply(entry.getValue()))).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            return iterator.apply(this, scope, record, recordData);
        }

        if (node instanceof SimpleExpression) {
            SimpleExpression expr = (SimpleExpression) node;
            Expression left = (Expression) this.apply(scope, expr.leftSide);
            Expression right = (Expression) this.apply(scope, expr.rightSide);

            return iterator.apply(this, scope, expr, left, right);
        }

        if (node instanceof TypeCheckExpression) {
            return iterator.apply(this, scope, (TypeCheckExpression) node);
        }

        if (node instanceof ValueClassEquality) {
            ValueClassEquality vce = (ValueClassEquality) node;
            Expression left = (Expression) this.apply(scope, vce.left);
            Expression right = (Expression) this.apply(scope, vce.right);

            return iterator.apply(this, scope, vce, left, right);
        }

        if (node instanceof RequiresNode) {
            return iterator.apply(this, scope, (RequiresNode) node);
        }

        if (node instanceof TailExtraction) {
            TailExtraction extr = (TailExtraction) node;
            Expression receiver = (Expression) this.apply(scope, extr.expression);

            return iterator.apply(this, scope, extr, receiver);
        }

        if (node instanceof BlockExpression) {
            BlockExpression block = (BlockExpression) node;
            Scope blockScope = scope.diveInIfNeeded(block);

            List<Expression> expr = block.expressions.stream().map(e -> this.apply(blockScope, e)).map(e -> (Expression) e).collect(Collectors.toList());

            return iterator.apply(this, scope, block, expr);
        }

        if (node instanceof LetConstant) {
            LetConstant letConstant = (LetConstant) node;
            Scope letScope = scope.diveInIfNeeded(node);

            Expression body = (Expression) this.apply(letScope, letConstant.body);
            return iterator.apply(this, scope, letConstant, body);
        }

        if (node instanceof LetFunction) {
            LetFunction letFn = (LetFunction) node;
            Scope letScope = scope.diveInIfNeeded(node);

            Expression body = (Expression) this.apply(letScope, letFn.body);
            return iterator.apply(this, scope, letFn, body);
        }

        if (node instanceof Lambda) {
            Lambda lambda = (Lambda) node;
            Expression body = (Expression) this.apply(scope, lambda.body);
            return iterator.apply(this, scope, lambda, body);
        }

        if (node instanceof IfElse) {
            IfElse ifElse = (IfElse) node;
            Expression condition = (Expression) this.apply(scope, ifElse.condition);
            Expression whenTrue = (Expression) this.apply(scope, ifElse.whenTrue);
            Expression whenFalse = ifElse.whenFalse != null ? (Expression) this.apply(scope, ifElse.whenFalse) : null;

            return iterator.apply(this, scope, ifElse, condition, whenTrue, whenFalse);
        }

        if (node instanceof Continuation) {
            Continuation continuation = (Continuation) node;
            Expression body = (Expression) this.apply(scope, continuation.body);
            return iterator.apply(this, scope, continuation, body);
        }

        if (node instanceof CommentNode || node instanceof RootNode || node == null) {
            return node;
        }

        throw new ParserException(node, "Unknown node");
    }

    @Override
    public String phase() {
        return phase;
    }
}
