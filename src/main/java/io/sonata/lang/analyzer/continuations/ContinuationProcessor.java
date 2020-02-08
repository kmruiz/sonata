/*
 * Copyright (c) 2020 Kevin Mas Ruiz
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package io.sonata.lang.analyzer.continuations;

import io.sonata.lang.analyzer.Processor;
import io.sonata.lang.analyzer.typeSystem.FunctionType;
import io.sonata.lang.analyzer.typeSystem.Scope;
import io.sonata.lang.log.CompilerLog;
import io.sonata.lang.parser.ast.Node;
import io.sonata.lang.parser.ast.ScriptNode;
import io.sonata.lang.parser.ast.classes.entities.EntityClass;
import io.sonata.lang.parser.ast.classes.values.ValueClass;
import io.sonata.lang.parser.ast.exp.*;
import io.sonata.lang.parser.ast.let.LetConstant;
import io.sonata.lang.parser.ast.let.LetFunction;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

public final class ContinuationProcessor implements Processor {
    private final CompilerLog log;
    private final Scope rootScope;

    public ContinuationProcessor(CompilerLog log, Scope rootScope) {
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
            return new ScriptNode(script.log, script.nodes.stream().map(n -> this.apply(scope, n)).collect(toList()), script.currentNode, script.requiresNotifier);
        }

        if (node instanceof FunctionCall) {
            FunctionCall fc = (FunctionCall) node;
            if (!(fc.receiver instanceof Atom)) {
                Expression receiver = (Expression) apply(scope, fc.receiver);
                fc = new FunctionCall(receiver, fc.arguments);
            }

            fc = new FunctionCall(fc.receiver, fc.arguments.stream().map(arg -> (Expression) apply(scope, arg)).collect(toList()));

            if (scope.inEntityClass() && shouldWaitForAllContinuations(scope, fc)) {
                return new Continuation(fc.definition(), fc, true);
            }

            if (isInferredTypeEntityClass(scope, fc)) {
                return new Continuation(fc.definition(), fc, false);
            }

            if (scope.inEntityClass() && !fc.receiver.representation().contains("some")) {
                return new Continuation(fc.definition(), fc, false);
            }

            if (fc.receiver instanceof Continuation) {
                return new Continuation(fc.definition(), fc, false);
            }

            return fc;
        }

        if (node instanceof MethodReference) {
            MethodReference ref = (MethodReference) node;
            return new MethodReference((Expression) apply(scope, ref.receiver), ref.methodName);
        }

        if (node instanceof EntityClass) {
            EntityClass ec = (EntityClass) node;
            List<Node> body = ec.body.stream().map(b -> apply(scope.diveInIfNeeded(ec), b)).collect(toList());
            return new EntityClass(ec.definition, ec.name, ec.definedFields, ec.implementingContracts, body);
        }

        if (node instanceof ValueClass) {
            ValueClass vc = (ValueClass) node;
            List<Node> body = vc.body.stream().map(b -> apply(scope.diveInIfNeeded(vc), b)).collect(toList());
            return new ValueClass(vc.definition, vc.name, vc.definedFields, body);
        }

        if (node instanceof BlockExpression) {
            BlockExpression bc = (BlockExpression) node;
            List<Expression> body = bc.expressions.stream().map(b -> apply(scope.diveInIfNeeded(bc), b)).map(b -> (Expression) b).collect(toList());

            return new BlockExpression(bc.definition, body);
        }

        if (node instanceof LetConstant) {
            LetConstant lc = (LetConstant) node;
            return new LetConstant(lc.definition, lc.letName, lc.returnType, (Expression) apply(scope, lc.body));
        }

        if (node instanceof LetFunction) {
            LetFunction lf = (LetFunction) node;
            Scope lfScope = scope.diveInIfNeeded(lf);

            return new LetFunction(lf.letId, lf.definition, lf.letName, lf.parameters, lf.returnType, (Expression) apply(lfScope, lf.body), false);
        }

        if (node instanceof Lambda) {
            Lambda ld = (Lambda) node;
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

        if (node instanceof SimpleExpression) {
            SimpleExpression expr = (SimpleExpression) node;
            return new SimpleExpression((Expression) apply(scope, expr.leftSide), expr.operator, (Expression) apply(scope, expr.rightSide));
        }

        if (node instanceof Record) {
            Record record = (Record) node;
            Map<Atom, Expression> recordData = record.values.entrySet().stream().map(entry -> new AbstractMap.SimpleImmutableEntry<>(entry.getKey(), (Expression) apply(scope, entry.getValue()))).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            return new Record(record.definition, recordData);
        }

        if (node instanceof LiteralArray) {
            LiteralArray array = (LiteralArray) node;
            return new LiteralArray(array.definition, array.expressions.stream().map(expr -> (Expression) apply(scope, expr)).collect(toList()));
        }

        return node;
    }

    private boolean shouldWaitForAllContinuations(Scope scope, FunctionCall fc) {
        return fc.receiver.representation().endsWith("map");
    }

    private boolean isInferredTypeEntityClass(Scope scope, Expression expression) {
        if (expression instanceof MethodReference) {
            MethodReference ref = (MethodReference) expression;

            if (ref.methodName.equals("some")) {
                return false;
            }

            if (ref.receiver instanceof Atom) {
                Atom receiver = (Atom) ref.receiver;
                if (receiver.kind != Atom.Kind.IDENTIFIER) {
                    return false;
                }

                final Optional<Scope.Variable> variable = scope.resolveVariable(receiver.value);
                return variable.map(v -> v.type.isEntity()).orElse(false);
            }

            if (ref.receiver instanceof MethodReference) {
                return isInferredTypeEntityClass(scope, ref.receiver);
            }

            if (ref.receiver instanceof Continuation) {
                return true;
            }
        }

        if (expression instanceof FunctionCall) {
            final FunctionCall fc = (FunctionCall) expression;
            if (fc.receiver instanceof Atom) {
                final Atom name = (Atom) fc.receiver;

                return scope.resolveVariable(name.value)
                        .filter(variable -> variable.type instanceof FunctionType)
                        .map(variable -> (FunctionType) variable.type)
                        .map(fcType -> fcType.returnType.isEntity())
                        .orElse(false);
            }

            return isInferredTypeEntityClass(scope, fc.receiver);
        }

        if (expression instanceof Atom) {
            return expression.representation().equals("self") && scope.inEntityClass();
        }

        return false;
    }

    @Override
    public String phase() {
        return "CONTINUATIONS";
    }
}
