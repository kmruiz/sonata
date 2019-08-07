package io.sonata.lang.backend;

import io.sonata.lang.parser.ast.Node;
import io.sonata.lang.parser.ast.ScriptNode;
import io.sonata.lang.parser.ast.classes.values.ValueClass;
import io.sonata.lang.parser.ast.exp.*;
import io.sonata.lang.parser.ast.let.LetFunction;
import io.sonata.lang.parser.ast.let.fn.SimpleParameter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class BackendVisitor implements BackendCodeGenerator {
    public interface BackendFactory {
        Backend newBackend();
    }

    private final BackendFactory backendFactory;

    public BackendVisitor(BackendFactory backend) {
        this.backendFactory = backend;
    }

    public byte[] generateSourceCode(Node node) {
        var backend = backendFactory.newBackend();
        visitTree(node, backend, new ArrayList<>(256));
        return backend.result();
    }

    private void visitTree(Node node, Backend backend, List<LetFunction> funcDefs) {
        if (node instanceof ScriptNode) {
            backend.emitScriptBegin((ScriptNode) node, this);
            ((ScriptNode) node).nodes.forEach(e -> visitTree(e, backend, funcDefs));
            emitFunctionList(backend, funcDefs);
            backend.emitScriptEnd((ScriptNode) node, this);
        }

        if (node instanceof LetFunction) {
            funcDefs.add((LetFunction) node);
        }

        if (node instanceof Atom) {
            backend.emitAtomExpressionBegin((Atom) node, this);
            backend.emitAtomExpressionEnd((Atom) node, this);
        }

        if (node instanceof SimpleExpression) {
            backend.emitSimpleExpressionBegin((SimpleExpression) node, this);
            visitTree(((SimpleExpression) node).leftSide, backend, funcDefs);
            backend.emitSimpleExpressionOperator(((SimpleExpression) node).operator, this);
            visitTree(((SimpleExpression) node).rightSide, backend, funcDefs);
            backend.emitSimpleExpressionEnd((SimpleExpression) node, this);
        }

        if (node instanceof PriorityExpression) {
            backend.emitPriorityExpressionBegin((PriorityExpression) node, this);
            visitTree(((PriorityExpression) node).expression, backend, funcDefs);
            backend.emitPriorityExpressionEnd((PriorityExpression) node, this);
        }

        if (node instanceof FunctionCall) {
            backend.emitPreFunctionCall((FunctionCall) node, this);
            visitTree(((FunctionCall) node).receiver, backend, funcDefs);
            backend.emitFunctionCallBegin((FunctionCall) node, this);
            AtomicInteger len = new AtomicInteger(((FunctionCall) node).arguments.size());
            ((FunctionCall) node).arguments.forEach(arg -> {
                len.decrementAndGet();
                backend.emitFunctionCallArgumentBegin(arg, len.get() == 0, this);
                visitTree(arg, backend, funcDefs);
                backend.emitFunctionCallArgumentEnd(arg, len.get() == 0, this);
            });
            backend.emitFunctionCallEnd((FunctionCall) node, this);
            backend.emitPostFunctionCall((FunctionCall) node, this);
        }

        if (node instanceof ValueClass) {
            backend.emitPreValueClass((ValueClass) node, this);
            AtomicInteger len = new AtomicInteger(((ValueClass) node).definedFields.size());
            ((ValueClass) node).definedFields.forEach(field -> {
                len.decrementAndGet();
                backend.emitValueClassFieldBegin((ValueClass) node, field, len.get() == 0, this);
                backend.emitValueClassFieldEnd((ValueClass) node, field, len.get() == 0, this);
            });
            backend.emitPostValueClass((ValueClass) node, this);
            backend.emitValueClassBodyBegin((ValueClass) node, this);
            var defs = new ArrayList<LetFunction>(256);
            ((ValueClass) node).body.forEach(expr -> {
                visitTree(expr, backend, defs);
            });
            emitFunctionList(backend, defs);
            backend.emitValueClassBodyEnd((ValueClass) node, this);
        }

        if (node instanceof LiteralArray) {
            backend.emitArrayBegin((LiteralArray) node, this);
            AtomicInteger len = new AtomicInteger(((LiteralArray) node).expressions.size());
            ((LiteralArray) node).expressions.forEach(arg -> {
                len.decrementAndGet();
                visitTree(arg, backend, funcDefs);
                backend.emitArraySeparator((LiteralArray) node, len.get() == 0, this);
            });
            backend.emitArrayEnd((LiteralArray) node, this);
        }

        if (node instanceof MethodReference) {
            backend.emitMethodReferenceBegin((MethodReference) node, this);
            visitTree(((MethodReference) node).receiver, backend, funcDefs);
            backend.emitMethodReferenceName(((MethodReference) node).methodName, this);
            backend.emitMethodReferenceEnd((MethodReference) node, this);
        }

        if (node instanceof ArrayAccess) {
            backend.emitArrayAccessBegin((ArrayAccess) node, this);
            visitTree(((ArrayAccess) node).receiver, backend, funcDefs);
            backend.emitArrayAccessIndex(((ArrayAccess) node).index, this);
            backend.emitArrayAccessEnd((ArrayAccess) node, this);
        }
    }

    private void emitFunctionList(Backend backend, List<LetFunction> funcDefs) {
        var funcMap = funcDefs.stream().collect(Collectors.groupingBy(v -> v.letName));
        funcMap.values().forEach(letFns -> {
            var base = letFns.stream().filter(e -> e.parameters.stream().allMatch(x -> x instanceof SimpleParameter)).findFirst().get();
            var rest = letFns.stream().filter(e -> !e.parameters.stream().allMatch(x -> x instanceof SimpleParameter)).collect(Collectors.toList());

            backend.emitFunctionDefinitionBegin(letFns, this);

            rest.forEach(spec -> {
                backend.emitFunctionSpecificationBegin(spec, this);
                visitTree(spec.body, backend, funcDefs);
                backend.emitFunctionSpecificationEnd(spec, this);
            });

            backend.emitBaseFunctionSpecificationBegin(base, this);
            visitTree(base.body, backend, funcDefs);
            backend.emitBaseFunctionSpecificationEnd(base, this);

            backend.emitFunctionDefinitionEnd(letFns, this);
        });
    }

    @Override
    public byte[] generateFor(Node node) {
        return new BackendVisitor(backendFactory).generateSourceCode(node);
    }
}
