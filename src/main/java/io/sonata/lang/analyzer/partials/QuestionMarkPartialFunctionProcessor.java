/*
 * Copyright (c) 2020 Kevin Mas Ruiz
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package io.sonata.lang.analyzer.partials;

import io.sonata.lang.analyzer.Processor;
import io.sonata.lang.parser.ast.Node;
import io.sonata.lang.parser.ast.ScriptNode;
import io.sonata.lang.parser.ast.classes.entities.EntityClass;
import io.sonata.lang.parser.ast.classes.values.ValueClass;
import io.sonata.lang.parser.ast.exp.*;
import io.sonata.lang.parser.ast.let.LetConstant;
import io.sonata.lang.parser.ast.let.LetFunction;
import io.sonata.lang.parser.ast.let.fn.SimpleParameter;
import io.sonata.lang.parser.ast.type.BasicASTTypeRepresentation;
import io.sonata.lang.source.SourcePosition;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public final class QuestionMarkPartialFunctionProcessor implements Processor {
    @Override
    public Node apply(Node node) {
        if (node instanceof ScriptNode) {
            ScriptNode script = (ScriptNode) node;
            return new ScriptNode(script.log, script.nodes.stream().map(this::apply).collect(Collectors.toList()), script.currentNode, script.requiresNotifier);
        }

        if (node instanceof FunctionCall) {
            return processAnonymousParametersOnFunctionCall((FunctionCall) node);
        }

        if (node instanceof LetConstant) {
            LetConstant let = (LetConstant) node;
            return new LetConstant(let.definition(), let.letName, let.returnType, (Expression) apply(let.body));
        }

        if (node instanceof LetFunction) {
            LetFunction let = (LetFunction) node;
            return new LetFunction(let.letId, let.definition, let.letName, let.parameters, let.returnType, (Expression) apply(let.body), false);
        }

        if (node instanceof ValueClass) {
            ValueClass vc = (ValueClass) node;
            List<Node> body = vc.body.stream().map(this::apply).collect(Collectors.toList());
            return new ValueClass(vc.definition, vc.name, vc.definedFields, body);
        }

        if (node instanceof EntityClass) {
            EntityClass entity = (EntityClass) node;
            List<Node> body = entity.body.stream().map(this::apply).collect(Collectors.toList());
            return new EntityClass(entity.definition, entity.name, entity.definedFields, body);
        }

        if (node instanceof BlockExpression) {
            BlockExpression block = (BlockExpression) node;
            List<Expression> body = block.expressions.stream().map(this::apply).map(e -> (Expression) e).collect(Collectors.toList());
            return new BlockExpression(block.definition, body);
        }

        if (node instanceof Expression) {
            return buildLambdaIfNeeded((Expression) node);
        }

        return node;
    }

    private Node processAnonymousParametersOnFunctionCall(FunctionCall fc) {
        List<Expression> args = fc.arguments.stream().map(this::buildLambdaIfNeeded).collect(Collectors.toList());
        return new FunctionCall(fc.receiver, args);
    }

    private Expression buildLambdaIfNeeded(Expression expression) {
        ArrayList<SimpleParameter> lambdaParams = new ArrayList<SimpleParameter>(4);
        Supplier<String> paramNameSupplier = scopedLambdaParameterNameSupplier(expression.definition(), lambdaParams);
        Expression newExpression = parseExpressionForLambda(expression, paramNameSupplier);

        if (lambdaParams.isEmpty()) {
            return expression;
        }

        return Lambda.synthetic(expression.definition(), lambdaParams, newExpression);
    }
    private Expression parseExpressionForLambda(Expression expression, Supplier<String> paramNameSupplier) {
        if (expression instanceof SimpleExpression) {
            SimpleExpression se = (SimpleExpression) expression;

            Expression left = parseExpressionForLambda(se.leftSide, paramNameSupplier);
            String operator = se.operator;
            Expression right = parseExpressionForLambda(se.rightSide, paramNameSupplier);

            return new SimpleExpression(left, operator, right);
        } else if (expression instanceof Atom) {
            if (Atom.isUnknownAtom(expression)) {
                return new Atom(expression.definition(), paramNameSupplier.get());
            }
        } else if (expression instanceof MethodReference) {
            MethodReference ref = (MethodReference) expression;
            Expression receiver = parseExpressionForLambda(ref.receiver, paramNameSupplier);
            return new MethodReference(receiver, ref.methodName);
        } else if (expression instanceof FunctionCall) {
            FunctionCall fc = (FunctionCall) expression;
            Expression rcv = parseExpressionForLambda(fc.receiver, paramNameSupplier);
            List<Expression> args = fc.arguments.stream().map(e -> parseExpressionForLambda(e, paramNameSupplier)).collect(Collectors.toList());
            return new FunctionCall(rcv, args);
        }

        return expression;
    }

    private Supplier<String> scopedLambdaParameterNameSupplier(SourcePosition definition, List<SimpleParameter> lambdaParams) {
        AtomicInteger counter = new AtomicInteger(0);
        char base = 'a';

        return () -> {
            String name = String.valueOf((char) (base + counter.getAndIncrement()));
            lambdaParams.add(new SimpleParameter(definition, name, new BasicASTTypeRepresentation(definition, "any"), SimpleParameter.State.END));

            return name;
        };
    }

    @Override
    public String phase() {
        return "ANON. PARTIAL FUNCTIONS";
    }
}
