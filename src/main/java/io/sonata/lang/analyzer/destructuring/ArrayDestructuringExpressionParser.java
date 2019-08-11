package io.sonata.lang.analyzer.destructuring;

import io.sonata.lang.parser.ast.Node;
import io.sonata.lang.parser.ast.exp.*;
import io.sonata.lang.parser.ast.let.LetConstant;
import io.sonata.lang.parser.ast.let.LetFunction;
import io.sonata.lang.parser.ast.let.fn.ExpressionParameter;
import io.sonata.lang.parser.ast.let.fn.Parameter;
import io.sonata.lang.parser.ast.let.fn.SimpleParameter;
import io.sonata.lang.parser.ast.type.BasicType;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Stream;

public class ArrayDestructuringExpressionParser implements DestructuringExpressionParser {
    private final class ArrayParameterTouchPoint {
        public final String arrayName;
        public final LiteralArray literalArray;

        private ArrayParameterTouchPoint(String arrayName, LiteralArray literalArray) {
            this.arrayName = arrayName;
            this.literalArray = literalArray;
        }
    }

    @Override
    public Stream<Node> createDestructuringExpression(String parameterName, Parameter parameter) {
        var argIdx = new AtomicInteger(0);
        return whenIsArrayParameter(parameterName, parameter, tp -> tp.literalArray.expressions.stream().map(e -> {
            var idx = argIdx.getAndIncrement();
            var value = (e instanceof Atom && ((Atom) e).type == Atom.Type.IDENTIFIER) ? e.representation() : identifierFor(idx);
            return new LetConstant(value, null, new ArrayAccess(new Atom(tp.arrayName), String.valueOf(idx)));
        }));
    }

    @Override
    public Parameter normalizeParameter(String parameterName, Parameter parameter) {
        var argIdx = new AtomicInteger(0);
        Parameter newParam = whenIsArrayParameter(parameterName, parameter, tp -> new SimpleParameter(identifierFor(argIdx.getAndIncrement()), null, SimpleParameter.State.END));
        if (newParam != null) {
            return newParam;
        }

        return parameter;
    }

    @Override
    public Stream<Expression> generateGuardCondition(String parameterName, Parameter parameter) {
        var argIdx = new AtomicInteger(0);
        return whenIsArrayParameter(parameterName, parameter, tp -> tp.literalArray.expressions.stream().map(arg -> {
            var idx = argIdx.getAndIncrement();
            var value = (arg instanceof Atom && ((Atom) arg).type == Atom.Type.IDENTIFIER) ? arg.representation() : identifierFor(idx);

            if (arg instanceof Atom) {
                var atom = ((Atom) arg);
                if (atom.type != Atom.Type.IDENTIFIER) {
                    return new SimpleExpression(new Atom(value), "===", arg);
                }
            } else if (arg instanceof Expression) {
                return arg;
            }

            return null;
        }));
    }

    private <T> T whenIsArrayParameter(String parameterName, Parameter parameter, Function<ArrayParameterTouchPoint, T> fn) {
        if (parameter instanceof ExpressionParameter) {
            if (((ExpressionParameter) parameter).expression instanceof LiteralArray) {
                var literal = ((LiteralArray) ((ExpressionParameter) parameter).expression);
                return fn.apply(new ArrayParameterTouchPoint(parameterName, literal));
            }
        }

        return null;
    }

    private static String identifierFor(int id) {
        return String.valueOf((char) (id + 'a'));
    }
}
