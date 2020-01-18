/*
 * Copyright (c) 2020 Kevin Mas Ruiz
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package io.sonata.lang.analyzer.fops;

import io.sonata.lang.analyzer.Processor;
import io.sonata.lang.analyzer.typeSystem.FunctionType;
import io.sonata.lang.analyzer.typeSystem.Scope;
import io.sonata.lang.exception.SonataSyntaxError;
import io.sonata.lang.log.CompilerLog;
import io.sonata.lang.parser.ast.Node;
import io.sonata.lang.parser.ast.ScriptNode;
import io.sonata.lang.parser.ast.classes.entities.EntityClass;
import io.sonata.lang.parser.ast.classes.values.ValueClass;
import io.sonata.lang.parser.ast.exp.*;
import io.sonata.lang.parser.ast.let.LetConstant;
import io.sonata.lang.parser.ast.let.LetFunction;
import io.sonata.lang.parser.ast.let.fn.SimpleParameter;

import java.util.Collections;
import java.util.List;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

public class FunctionCompositionProcessor implements Processor {
    private final CompilerLog log;
    private final Scope scope;

    public FunctionCompositionProcessor(CompilerLog log, Scope scope) {
        this.log = log;
        this.scope = scope;
    }

    @Override
    public Node apply(Node node) {
        if (node instanceof ScriptNode) {
            ScriptNode script = (ScriptNode) node;
            return new ScriptNode(script.log, script.nodes.stream().map(this::apply).collect(toList()), script.currentNode, script.requiresNotifier);
        }

        if (node instanceof EntityClass) {
            EntityClass entityClass = (EntityClass) node;
            return new EntityClass(entityClass.definition, entityClass.name, entityClass.definedFields, entityClass.body.stream().map(this::apply).collect(toList()));
        }

        if (node instanceof ValueClass) {
            ValueClass valueClass = (ValueClass) node;
            return new ValueClass(valueClass.definition, valueClass.name, valueClass.definedFields, valueClass.body.stream().map(this::apply).collect(toList()));
        }

        if (node instanceof LetFunction) {
            LetFunction fn = (LetFunction) node;
            return new LetFunction(fn.letId, fn.definition, fn.letName, fn.parameters, fn.returnType, (Expression) apply(fn.body));
        }

        if (node instanceof LetConstant) {
            LetConstant constant = (LetConstant) node;
            return new LetConstant(constant.definition, constant.letName, constant.returnType, (Expression) apply(constant.body));
        }

        if (node instanceof Lambda) {
            Lambda lambda = (Lambda) node;
            return new Lambda(lambda.lambdaId, lambda.definition, lambda.parameters, (Expression) apply(lambda.body));
        }

        if (node instanceof IfElse) {
            IfElse ifElse = (IfElse) node;
            return new IfElse(ifElse.definition, (Expression) apply(ifElse.condition), (Expression) apply(ifElse.whenTrue), (Expression) apply(ifElse.whenFalse));
        }

        if (node instanceof BlockExpression) {
            BlockExpression block = (BlockExpression) node;
            return new BlockExpression(block.blockId, block.definition, block.expressions.stream().map(this::apply).map(e -> (Expression) e).collect(toList()));
        }

        if (node instanceof SimpleExpression) {
            return composeIfNeeded((SimpleExpression) node);
        }

        if (node instanceof FunctionCall) {
            FunctionCall fc = (FunctionCall) node;
            return new FunctionCall(fc.receiver, fc.arguments.stream().map(this::apply).map(e -> (Expression) e).collect(toList()), fc.expressionType);
        }

        return node;
    }

    private Node composeIfNeeded(SimpleExpression expression) {
        if (!expression.operator.equals("->")) {
            return expression;
        }

        if (isFunction(expression.leftSide) && isFunction(expression.rightSide)) {
            List<SimpleParameter> newFnParams = parametersOf(expression.leftSide);
            return compositionOf(newFnParams, (Expression) apply(expression.leftSide), (Expression) apply(expression.rightSide));
        }

        log.syntaxError(new SonataSyntaxError(expression, "Only functions can be composed."));
        return expression;
    }

    private boolean isFunction(Expression expr) {
        if (expr instanceof Lambda) {
            return true;
        }

        if (expr instanceof Atom) {
            final Scope.Variable variable = scope.resolveVariable(((Atom) expr).value).get();
            return variable.type instanceof FunctionType;
        }

        if (expr instanceof SimpleExpression) {
            SimpleExpression sexpr = (SimpleExpression) expr;
            return isFunction(sexpr.leftSide) && isFunction(sexpr.rightSide) && sexpr.operator.equals("->");
        }

        return false;
    }

    private List<SimpleParameter> parametersOf(Expression expr) {
        if (expr instanceof Atom) {
            final Scope.Variable variable = scope.resolveVariable(((Atom) expr).value).get();
            FunctionType fnType = (FunctionType) variable.type;
            return fnType.namedParameters();
        }

        if (expr instanceof Lambda) {
            return ((Lambda) expr).parameters;
        }

        return Collections.emptyList();
    }

    private Expression compositionOf(List<SimpleParameter> params, Expression a, Expression b) {
        return Lambda.synthetic(a.definition(), params,
                new FunctionCall(b, singletonList(
                        new FunctionCall(a,
                                params.stream().map(p -> new Atom(p.definition, p.name)).collect(toList())
                        )
                ))
        );
    }

    @Override
    public String phase() {
        return "FUNCTION COMPOSITION";
    }
}
