package io.sonata.lang.backend;

import io.sonata.lang.backend.js.JSBackend;
import io.sonata.lang.parser.Parser;
import io.sonata.lang.parser.ast.Node;
import io.sonata.lang.parser.ast.ScriptNode;
import io.sonata.lang.parser.ast.exp.*;
import io.sonata.lang.parser.ast.let.LetFunction;
import io.sonata.lang.parser.ast.let.fn.SimpleParameter;
import io.sonata.lang.source.Source;
import io.sonata.lang.tokenizer.Tokenizer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class BackendVisitor implements BackendCodeGenerator {
    public interface BackendFactory {
        Backend newBackend();
    }
    public final BackendFactory backendFactory;
    public final List<LetFunction> funcDefs;

    public BackendVisitor(BackendFactory backend) {
        this.backendFactory = backend;
        this.funcDefs = new ArrayList<>(256);
    }

    public byte[] generateSourceCode(Node node) {
        var backend = backendFactory.newBackend();
        visitTree(node, backend);
        return backend.result();
    }

    private void visitTree(Node node, Backend backend) {
        if (node instanceof ScriptNode) {
            backend.emitScriptBegin((ScriptNode) node, this);
            ((ScriptNode) node).nodes.forEach(e -> visitTree(e, backend));

            var funcMap = funcDefs.stream().collect(Collectors.groupingBy(v -> v.letName));
            funcMap.values().forEach(letFns -> {
                var base = letFns.stream().filter(e -> e.parameters.stream().allMatch(x -> x instanceof SimpleParameter)).findFirst().get();
                var rest = letFns.stream().filter(e -> !e.parameters.stream().allMatch(x -> x instanceof SimpleParameter)).collect(Collectors.toList());

                backend.emitFunctionDefinitionBegin(letFns, this);

                rest.forEach(spec -> {
                    backend.emitFunctionSpecificationBegin(spec, this);
                    visitTree(spec.body, backend);
                    backend.emitFunctionSpecificationEnd(spec, this);
                });

                backend.emitBaseFunctionSpecificationBegin(base, this);
                visitTree(base.body, backend);
                backend.emitBaseFunctionSpecificationEnd(base, this);

                backend.emitFunctionDefinitionEnd(letFns, this);
            });

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
            visitTree(((SimpleExpression) node).leftSide, backend);
            backend.emitSimpleExpressionOperator(((SimpleExpression) node).operator, this);
            visitTree(((SimpleExpression) node).rightSide, backend);
            backend.emitSimpleExpressionEnd((SimpleExpression) node, this);
        }

        if (node instanceof PriorityExpression) {
            backend.emitPriorityExpressionBegin((PriorityExpression) node, this);
            visitTree(((PriorityExpression) node).expression, backend);
            backend.emitPriorityExpressionEnd((PriorityExpression) node, this);
        }

        if (node instanceof FunctionCall) {
            visitTree(((FunctionCall) node).receiver, backend);
            backend.emitFunctionCallBegin((FunctionCall) node, this);
            AtomicInteger len = new AtomicInteger(((FunctionCall) node).arguments.size());
            ((FunctionCall) node).arguments.forEach(arg -> {
                len.decrementAndGet();
                backend.emitFunctionCallArgumentBegin(arg, len.get() == 0, this);
                visitTree(arg, backend);
                backend.emitFunctionCallArgumentEnd(arg, len.get() == 0, this);
            });
            backend.emitFunctionCallEnd((FunctionCall) node, this);
        }

        if (node instanceof MethodReference) {
            backend.emitMethodReferenceBegin((MethodReference) node, this);
            visitTree(((MethodReference) node).receiver, backend);
            backend.emitMethodReferenceName(((MethodReference) node).methodName, this);
            backend.emitMethodReferenceEnd((MethodReference) node, this);
        }
    }

    @Override
    public byte[] generateFor(Node node) {
        return new BackendVisitor(backendFactory).generateSourceCode(node);
    }
}
