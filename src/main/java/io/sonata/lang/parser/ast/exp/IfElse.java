/*
 * Copyright (c) 2020 Kevin Mas Ruiz
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package io.sonata.lang.parser.ast.exp;

import io.sonata.lang.source.SourcePosition;

public class IfElse extends ComposedExpression {
    public final SourcePosition definition;
    public final Expression condition;
    public final Expression whenTrue;
    public final Expression whenFalse;

    public IfElse(SourcePosition definition, Expression condition, Expression whenTrue, Expression whenFalse) {
        this.definition = definition;
        this.condition = condition;
        this.whenTrue = whenTrue;
        this.whenFalse = whenFalse;
    }

    public int weight() {
        return weightOf(condition);
    }

    private int weightOf(Expression expr) {
        if (expr instanceof SimpleExpression) {
            return weightOf(((SimpleExpression) expr).leftSide) + weightOf(((SimpleExpression) expr).rightSide);
        }

        return 1;
    }

    @Override
    public String representation() {
        return "if (" + representationOrEmpty(condition) + ") { " + representationOrEmpty(whenTrue) + " } else { "  + representationOrEmpty(whenFalse) + " }";
    }

    private String representationOrEmpty(Expression expr) {
        return expr != null ? expr.representation() : "<none>";
    }

    public static int weightedComparison(Expression a, Expression b) {
        if (a instanceof IfElse && b instanceof IfElse) {
            return ((IfElse) b).weight() - ((IfElse) a).weight();
        }

        if (a instanceof IfElse) {
            return -1;
        }

        if (b instanceof IfElse) {
            return 1;
        }

        return 0;
    }

    @Override
    public SourcePosition definition() {
        return definition;
    }
}
