package io.sonata.lang.analyzer.partials;

import io.sonata.lang.analyzer.Processor;
import io.sonata.lang.parser.ast.Node;
import io.sonata.lang.parser.ast.ScriptNode;
import io.sonata.lang.parser.ast.exp.*;
import io.sonata.lang.parser.ast.let.LetConstant;
import io.sonata.lang.parser.ast.let.fn.SimpleParameter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class QuestionMarkPartialFunctionProcessor implements Processor {
    @Override
    public Node apply(Node node) {
        if (node instanceof ScriptNode) {
            ScriptNode script = (ScriptNode) node;
            return new ScriptNode(script.nodes.stream().map(this::apply).collect(Collectors.toList()), script.currentNode, script.requiresNotifier);
        }

        if (node instanceof FunctionCall) {
            return processAnonymousParametersOnFunctionCall((FunctionCall) node);
        }

        if (node instanceof LetConstant) {
            LetConstant let = (LetConstant) node;
            return new LetConstant(let.letName, let.returnType, (Expression) apply(let.body));
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
        Supplier<String> paramNameSupplier = scopedLambdaParameterNameSupplier(lambdaParams);
        Expression newExpression = parseExpressionForLambda(expression, paramNameSupplier);

        if (lambdaParams.isEmpty()) {
            return expression;
        }

        return Lambda.synthetic(lambdaParams, newExpression);
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
                return new Atom(paramNameSupplier.get());
            }
        }

        return expression;
    }

    private Supplier<String> scopedLambdaParameterNameSupplier(List<SimpleParameter> lambdaParams) {
        AtomicInteger counter = new AtomicInteger(0);
        char base = 'a';

        return () -> {
            String name = String.valueOf((char) (base + counter.getAndIncrement()));
            lambdaParams.add(new SimpleParameter(name, null, SimpleParameter.State.END));

            return name;
        };
    }
}
