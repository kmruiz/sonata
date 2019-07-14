package io.sonata.lang.parser.ast.exp;

public class MethodReference extends ComposedExpression implements Expression {
    public final Expression receiver;
    public final String methodName;

    public MethodReference(Expression receiver, String methodName) {
        this.receiver = receiver;
        this.methodName = methodName;
    }

    @Override
    public String representation() {
        return receiver.representation() + "." + methodName;
    }
}
