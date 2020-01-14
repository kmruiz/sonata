/*
 * Copyright (c) 2020 Kevin Mas Ruiz
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package io.sonata.lang.analyzer.typeSystem;

import io.sonata.lang.analyzer.Processor;
import io.sonata.lang.exception.SonataSyntaxError;
import io.sonata.lang.log.CompilerLog;
import io.sonata.lang.parser.ast.Node;
import io.sonata.lang.parser.ast.ScriptNode;
import io.sonata.lang.parser.ast.classes.entities.EntityClass;
import io.sonata.lang.parser.ast.classes.values.ValueClass;
import io.sonata.lang.parser.ast.exp.*;
import io.sonata.lang.parser.ast.let.LetConstant;
import io.sonata.lang.parser.ast.let.LetFunction;

public class ImmutabilityCheckProcessor implements Processor {
    private final CompilerLog log;
    private final Scope scope;

    public ImmutabilityCheckProcessor(CompilerLog log, Scope scope) {
        this.log = log;
        this.scope = scope;
    }

    @Override
    public Node apply(Node node) {
        apply(scope, node);
        return node;
    }

    private void apply(Scope scope, Node node) {
        if (node instanceof ScriptNode) {
            ScriptNode script = (ScriptNode) node;
            script.nodes.parallelStream().forEach(n -> this.apply(scope.diveInIfNeeded(n), n));
        }

        if (node instanceof EntityClass) {
            EntityClass entityClass = (EntityClass) node;
            entityClass.body.parallelStream().forEach(b -> this.apply(scope.diveInIfNeeded(b), b));
        }

        if (node instanceof ValueClass) {
            ValueClass valueClass = (ValueClass) node;
            valueClass.body.parallelStream().forEach(b -> this.apply(scope.diveInIfNeeded(b), b));
        }

        if (node instanceof LetFunction) {
            LetFunction fn = (LetFunction) node;
            fn.parameters.parallelStream().forEach(b -> this.apply(scope.diveInIfNeeded(b), b));
            apply(scope.diveInIfNeeded(fn.body), fn.body);
        }

        if (node instanceof LetConstant) {
            LetConstant constant = (LetConstant) node;
            apply(scope.diveInIfNeeded(constant.body), constant.body);
        }

        if (node instanceof Lambda) {
            Lambda lambda = (Lambda) node;
            apply(scope.diveInIfNeeded(lambda.body), lambda.body);
        }

        if (node instanceof IfElse) {
            IfElse ifElse = (IfElse) node;
            apply(scope.diveInIfNeeded(ifElse.condition), ifElse.condition);
            apply(scope.diveInIfNeeded(ifElse.whenTrue), ifElse.whenTrue);
            apply(scope.diveInIfNeeded(ifElse.whenFalse), ifElse.whenFalse);
        }

        if (node instanceof BlockExpression) {
            BlockExpression block = (BlockExpression) node;
            block.expressions.parallelStream().forEach(b -> this.apply(scope.diveInIfNeeded(b), b));
        }

        if (node instanceof SimpleExpression) {
            SimpleExpression expr = (SimpleExpression) node;
            if (expr.operator.equals("=")) {
                validate(scope.diveInIfNeeded(expr.leftSide), expr.leftSide);
            }

            apply(scope.diveInIfNeeded(expr.leftSide), expr.leftSide);
            apply(scope.diveInIfNeeded(expr.rightSide), expr.rightSide);
        }

        if (node instanceof FunctionCall) {
            FunctionCall fc = (FunctionCall) node;
            apply(scope.diveInIfNeeded(fc.receiver), fc.receiver);
            fc.arguments.parallelStream().forEach(b -> this.apply(scope.diveInIfNeeded(b), b));
        }

        if (node instanceof MethodReference) {
            MethodReference ref = (MethodReference) node;
            apply(scope.diveInIfNeeded(ref.receiver), ref.receiver);
        }
    }

    private void validate(Scope scope, Expression expr) {
        if (expr instanceof Atom) {
            log.syntaxError(new SonataSyntaxError(expr, "Let constants can not be changed."));
        }

        if (expr instanceof MethodReference) {
            MethodReference ref = (MethodReference) expr;
            if (!isEntity(scope, expr)) {
                log.syntaxError(new SonataSyntaxError(ref, "Value objects, built using a value class, are immutable."));
            }
        }
    }

    private boolean isEntity(Scope scope, Expression expression) {
        if (expression instanceof Atom) {
            Atom atom = (Atom) expression;
            final Scope.Variable variable = scope.resolveVariable(atom.value).get();

            return variable.type.isEntity();
        }

        return false;
    }

    @Override
    public String phase() {
        return "IMMUTABILITY CHECKER";
    }
}
