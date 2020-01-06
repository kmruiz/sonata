/*
 * Copyright (c) 2020 Kevin Mas Ruiz
 *
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
import java.util.Optional;
import java.util.stream.Collectors;

public class ContinuationProcessor implements Processor {
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
            return new ScriptNode(script.log, script.nodes.stream().map(n -> this.apply(scope, n)).collect(Collectors.toList()), script.currentNode, script.requiresNotifier);
        }

        if (node instanceof FunctionCall) {
            FunctionCall fc = (FunctionCall) node;
            if (isInferredTypeEntityClass(scope, fc.receiver)) {
                return new Continuation(fc.definition(), fc);
            }
        }

        if (node instanceof EntityClass) {
            EntityClass ec = (EntityClass) node;
            List<Node> body = ec.body.stream().map(b -> apply(scope.diveIn(ec), b)).collect(Collectors.toList());
            return new EntityClass(ec.definition, ec.name, ec.definedFields, body);
        }

        if (node instanceof ValueClass) {
            ValueClass vc = (ValueClass) node;
            List<Node> body = vc.body.stream().map(b -> apply(scope.diveIn(vc), b)).collect(Collectors.toList());
            return new EntityClass(vc.definition, vc.name, vc.definedFields, body);
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

            return new LetFunction(lf.letId, lf.definition, lf.letName, lf.parameters, lf.returnType, (Expression) apply(lfScope, lf.body));
        }

        if (node instanceof Lambda) {
            Lambda ld = (Lambda) node;
            return new Lambda(ld.definition, ld.parameters, (Expression) apply(scope.diveIn(ld), ld.body));
        }

        if (node instanceof IfElse) {
            IfElse ie = (IfElse) node;
            Expression condition = (Expression) apply(scope, ie.condition);
            Expression whenTrue = (Expression) apply(scope, ie.whenTrue);

            if (ie.whenFalse == null) {
                return new IfElse(ie.ifElseId, ie.definition, condition, whenTrue,null);
            }

            Expression whenFalse = (Expression) apply(scope, ie.whenFalse);
            return new IfElse(ie.ifElseId, ie.definition, condition, whenTrue, whenFalse);
        }

        return node;
    }

    private boolean isInferredTypeEntityClass(Scope scope, Expression expression) {
        if (expression instanceof MethodReference) {
            return isMethodReferencingAnEntity(scope, (MethodReference) expression);
        }

        return false;
    }

    private boolean isMethodReferencingAnEntity(Scope scope, MethodReference ref) {
        if (ref.receiver instanceof Atom) {
            Atom receiver = (Atom) ref.receiver;
            if (receiver.type != Atom.Type.IDENTIFIER) {
                return false;
            }

            final Optional<Scope.Variable> variable = scope.resolveVariable(receiver.value);
            return variable.map(v -> v.type.isEntity()).orElse(false);
        } else if (ref.receiver instanceof MethodReference) {
            isMethodReferencingAnEntity(scope, (MethodReference) ref.receiver);
        }

        return false;
    }

    @Override
    public String phase() {
        return "CONTINUATIONS";
    }
}
