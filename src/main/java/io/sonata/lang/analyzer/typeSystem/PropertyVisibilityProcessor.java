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

import java.util.Optional;

public class PropertyVisibilityProcessor implements Processor {
    private final Scope scope;
    private final CompilerLog log;

    public PropertyVisibilityProcessor(CompilerLog log, Scope scope) {
        this.scope = scope;
        this.log = log;
    }

    @Override
    public Node apply(Node node) {
        apply(scope, node, false);
        return node;
    }

    private void apply(Scope scope, Node node, boolean inFunctionCall) {
        if (node instanceof ScriptNode) {
            ScriptNode script = (ScriptNode) node;
            script.nodes.parallelStream().forEach(n -> this.apply(scope.diveInIfNeeded(n), n, false));
        }

        if (node instanceof EntityClass) {
            EntityClass entityClass = (EntityClass) node;
            entityClass.body.parallelStream().forEach(b -> this.apply(scope.diveInIfNeeded(b), b, false));
        }

        if (node instanceof ValueClass) {
            ValueClass valueClass = (ValueClass) node;
            valueClass.body.parallelStream().forEach(b -> this.apply(scope.diveInIfNeeded(b), b, false));
        }

        if (node instanceof LetFunction) {
            LetFunction fn = (LetFunction) node;
            fn.parameters.parallelStream().forEach(b -> this.apply(scope.diveInIfNeeded(b), b, false));
            apply(scope.diveInIfNeeded(fn.body), fn.body, false);
        }

        if (node instanceof LetConstant) {
            LetConstant constant = (LetConstant) node;
            apply(scope.diveInIfNeeded(constant.body), constant.body, false);
        }

        if (node instanceof Lambda) {
            Lambda lambda = (Lambda) node;
            apply(scope.diveInIfNeeded(lambda.body), lambda.body, false);
        }

        if (node instanceof IfElse) {
            IfElse ifElse = (IfElse) node;
            apply(scope.diveInIfNeeded(ifElse.condition), ifElse.condition, false);
            apply(scope.diveInIfNeeded(ifElse.whenTrue), ifElse.whenTrue, false);
            apply(scope.diveInIfNeeded(ifElse.whenFalse), ifElse.whenFalse, false);
        }

        if (node instanceof BlockExpression) {
            BlockExpression block = (BlockExpression) node;
            block.expressions.parallelStream().forEach(b -> this.apply(scope.diveInIfNeeded(b), b, false));
        }

        if (node instanceof SimpleExpression) {
            SimpleExpression expr = (SimpleExpression) node;
            apply(scope.diveInIfNeeded(expr.leftSide), expr.leftSide, false);
            apply(scope.diveInIfNeeded(expr.rightSide), expr.rightSide, false);
        }

        if (node instanceof FunctionCall) {
            FunctionCall fc = (FunctionCall) node;
            apply(scope.diveInIfNeeded(fc.receiver), fc.receiver, true);
            fc.arguments.parallelStream().forEach(b -> this.apply(scope.diveInIfNeeded(b), b, false));
        }

        if (node instanceof MethodReference) {
            MethodReference ref = (MethodReference) node;
            validate(scope, ref, inFunctionCall);
        }
    }

    private void validate(Scope scope, MethodReference ref, boolean inFunctionCall) {
        if (ref.receiver instanceof Atom) {
            Atom receiver = (Atom) ref.receiver;

            if (receiver.value.equals("self")) {
                return;
            }

            final Optional<Scope.Variable> variable = scope.resolveVariable(receiver.value);

            if (variable.isPresent() && variable.get().type.isEntity() && !inFunctionCall) {
                log.syntaxError(new SonataSyntaxError(ref, "It's forbidden to access properties of another entity instance."));
            }
        } else {
            apply(scope,ref.receiver,false);
        }
    }

    @Override
    public String phase() {
        return "PROPERTY VISIBILITY";
    }
}
