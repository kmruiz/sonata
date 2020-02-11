/*
 * Copyright (c) 2020 Kevin Mas Ruiz
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package io.sonata.lang.analyzer.continuations;

import io.sonata.lang.analyzer.Processor;
import io.sonata.lang.analyzer.typeSystem.Scope;
import io.sonata.lang.log.CompilerLog;
import io.sonata.lang.parser.ast.Node;
import io.sonata.lang.parser.ast.ScriptNode;
import io.sonata.lang.parser.ast.classes.entities.EntityClass;
import io.sonata.lang.parser.ast.classes.values.ValueClass;
import io.sonata.lang.parser.ast.exp.*;
import io.sonata.lang.parser.ast.let.LetConstant;
import io.sonata.lang.parser.ast.let.LetFunction;

import java.util.List;
import java.util.stream.Collectors;

public final class AsyncFunctionProcessor implements Processor {
    private final CompilerLog log;
    private final Scope rootScope;

    public AsyncFunctionProcessor(CompilerLog log, Scope rootScope) {
        this.log = log;
        this.rootScope = rootScope;
    }

    @Override
    public Node apply(Node node) {
        return apply(rootScope, node);
    }

    public Node apply(Scope scope, Node node) {
        if (node instanceof ScriptNode) {
            ScriptNode script = (ScriptNode) node;
            return new ScriptNode(script.log, script.nodes.stream().map(n -> this.apply(scope, n)).collect(Collectors.toList()), script.currentNode);
        }

        if (node instanceof FunctionCall) {
            FunctionCall fc = (FunctionCall) node;
            return new FunctionCall((Expression) apply(scope, fc.receiver), fc.arguments.stream().map(arg -> (Expression) apply(scope, arg)).collect(Collectors.toList()));
        }

        if (node instanceof MethodReference) {
            MethodReference ref = (MethodReference) node;
            return new MethodReference((Expression) apply(scope, ref.receiver), ref.methodName);
        }

        if (node instanceof EntityClass) {
            EntityClass ec = (EntityClass) node;
            List<Node> body = ec.body.stream().map(b -> apply(scope.diveIn(ec), b)).collect(Collectors.toList());
            return new EntityClass(ec.definition, ec.name, ec.definedFields, ec.implementingContracts, body);
        }

        if (node instanceof ValueClass) {
            ValueClass vc = (ValueClass) node;
            List<Node> body = vc.body.stream().map(b -> apply(scope.diveIn(vc), b)).collect(Collectors.toList());
            return new ValueClass(vc.definition, vc.name, vc.definedFields, body);
        }

        if (node instanceof BlockExpression) {
            BlockExpression bc = (BlockExpression) node;
            List<Expression> body = bc.expressions.stream().map(b -> apply(scope.diveIn(bc), b)).map(b -> (Expression) b).collect(Collectors.toList());

            return new BlockExpression(bc.definition, body);
        }

        if (node instanceof LetConstant) {
            LetConstant lc = (LetConstant) node;
            return new LetConstant(lc.definition, lc.letName, lc.returnType, (Expression) apply(scope, lc.body));
        }

        if (node instanceof LetFunction) {
            LetFunction lf = (LetFunction) node;
            Scope lfScope = scope.diveIn(lf);

            return new LetFunction(lf.letId, lf.definition, lf.letName, lf.parameters, lf.returnType, (Expression) apply(lfScope, lf.body), hasContinuations(lf.body), lf.isClassLevel);
        }

        if (node instanceof Lambda) {
            Lambda ld = (Lambda) node;
            if (hasContinuations(ld.body)) {
                return new Lambda(ld.lambdaId, ld.definition, ld.parameters, (Expression) apply(scope.diveInIfNeeded(ld), ld.body), true);
            }

            return new Lambda(ld.lambdaId, ld.definition, ld.parameters, (Expression) apply(scope.diveInIfNeeded(ld), ld.body), false);
        }

        if (node instanceof IfElse) {
            IfElse ie = (IfElse) node;
            Expression condition = (Expression) apply(scope, ie.condition);
            Expression whenTrue = (Expression) apply(scope, ie.whenTrue);

            if (ie.whenFalse == null) {
                return new IfElse(ie.ifElseId, ie.definition, condition, whenTrue, null);
            }

            Expression whenFalse = (Expression) apply(scope, ie.whenFalse);
            return new IfElse(ie.ifElseId, ie.definition, condition, whenTrue, whenFalse);
        }

        if (node instanceof Continuation) {
            Continuation cont = (Continuation) node;
            return new Continuation(cont.definition, (Expression) apply(scope.diveInIfNeeded(cont.body), cont.body), cont.fanOut);
        }

        return node;
    }

    private boolean hasContinuations(Expression node) {
        if (node == null) {
            return false;
        }

        if (node instanceof Continuation) {
            return true;
        }

        if (node instanceof FunctionCall) {
            FunctionCall fc = (FunctionCall) node;
            return hasContinuations(fc.receiver) || fc.arguments.stream().anyMatch(this::hasContinuations);
        }

        if (node instanceof MethodReference) {
            MethodReference ref = (MethodReference) node;
            return ref.receiver instanceof Continuation;
        }

        if (node instanceof BlockExpression) {
            BlockExpression bc = (BlockExpression) node;
            return bc.expressions.stream().anyMatch(this::hasContinuations);
        }

        if (node instanceof LetConstant) {
            LetConstant lc = (LetConstant) node;
            return hasContinuations(lc.body);
        }

        if (node instanceof LetFunction) {
            LetFunction lf = (LetFunction) node;
            return hasContinuations(lf.body);
        }

        if (node instanceof Lambda) {
            Lambda ld = (Lambda) node;
            return hasContinuations(ld.body);
        }

        if (node instanceof IfElse) {
            IfElse ie = (IfElse) node;
            return hasContinuations(ie.whenFalse) || hasContinuations(ie.whenTrue) || hasContinuations(ie.condition);
        }

        return false;
    }

    @Override
    public String phase() {
        return "ASYNC FUNCTIONS";
    }
}
