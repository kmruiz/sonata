/*
 * Copyright (c) 2020 Kevin Mas Ruiz
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package io.sonata.lang.analyzer.typeSystem;

import io.sonata.lang.analyzer.Processor;
import io.sonata.lang.analyzer.ProcessorIterator;
import io.sonata.lang.analyzer.ProcessorWrapper;
import io.sonata.lang.analyzer.typeSystem.exception.TypeCanNotBeReassignedException;
import io.sonata.lang.exception.SonataSyntaxError;
import io.sonata.lang.log.CompilerLog;
import io.sonata.lang.parser.ast.Node;
import io.sonata.lang.parser.ast.ScriptNode;
import io.sonata.lang.parser.ast.classes.contracts.Contract;
import io.sonata.lang.parser.ast.classes.entities.EntityClass;
import io.sonata.lang.parser.ast.classes.values.ValueClass;
import io.sonata.lang.parser.ast.exp.*;
import io.sonata.lang.parser.ast.let.LetConstant;
import io.sonata.lang.parser.ast.let.LetFunction;
import io.sonata.lang.parser.ast.requires.RequiresNode;
import io.sonata.lang.parser.ast.type.BasicASTTypeRepresentation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public final class ContractProcessor implements ProcessorIterator {
    private final CompilerLog log;

    public static Processor processorInstance(Scope scope, CompilerLog log) {
        return new ProcessorWrapper(scope, "CONTRACTS",
                new ContractProcessor(log)
        );
    }

    public ContractProcessor(CompilerLog log) {
        this.log = log;
    }

    @Override
    public Node apply(Processor processor, Scope scope, ScriptNode node, List<Node> body) {
        return new ScriptNode(node.log, body, node.currentNode);
    }

    @Override
    public Expression apply(Processor processor, Scope scope, FunctionCall node, Expression receiver, List<Expression> arguments, Node parent) {
        return new FunctionCall(receiver, arguments, node.expressionType);
    }

    @Override
    public Expression apply(Processor processor, Scope scope, MethodReference node, Expression receiver, Node parent) {
        return new MethodReference(receiver, node.methodName);
    }

    @Override
    public Node apply(Processor processor, Scope classScope, EntityClass entityClass, List<Node> body, Node parent) {
        return new EntityClass(entityClass.definition, entityClass.name, entityClass.definedFields, entityClass.implementingContracts, body);
    }

    @Override
    public Node apply(Processor processor, Scope classScope, ValueClass valueClass, List<Node> body, Node parent) {
        return new ValueClass(valueClass.definition, valueClass.name, valueClass.definedFields, body);
    }

    @Override
    public Node apply(Processor processor, Scope scope, Contract node, List<Node> body, Node parent) {
        if (verify(node)) {
            register(scope, node);
        }

        return new Contract(node.definition, node.name, body, node.extensions);
    }

    @Override
    public Expression apply(Processor processor, Scope scope, ArrayAccess node, Expression receiver, Node parent) {
        return new ArrayAccess(receiver, node.index);
    }

    @Override
    public Expression apply(Processor processor, Scope scope, Atom node, Node parent) {
        return node;
    }

    @Override
    public Expression apply(Processor processor, Scope scope, LiteralArray node, List<Expression> contents, Node parent) {
        return new LiteralArray(node.definition, contents);
    }

    @Override
    public Expression apply(Processor processor, Scope scope, PriorityExpression node, Expression content, Node parent) {
        return new PriorityExpression(content);
    }

    @Override
    public Expression apply(Processor processor, Scope scope, Record node, Map<Atom, Expression> values, Node parent) {
        return new Record(node.definition, values);
    }

    @Override
    public Expression apply(Processor processor, Scope scope, SimpleExpression node, Expression left, Expression right, Node parent) {
        return new SimpleExpression(left, node.operator, right);
    }

    @Override
    public Expression apply(Processor processor, Scope scope, TypeCheckExpression node, Node parent) {
        return node;
    }

    @Override
    public Expression apply(Processor processor, Scope scope, ValueClassEquality node, Expression left, Expression right, Node parent) {
        return new ValueClassEquality(left, right, node.negate);
    }

    @Override
    public Node apply(Processor processor, Scope scope, RequiresNode node, Node parent) {
        return node;
    }

    @Override
    public Expression apply(Processor processor, Scope scope, TailExtraction node, Expression receiver, Node parent) {
        return new TailExtraction(receiver, node.fromIndex);
    }

    @Override
    public Expression apply(Processor processor, Scope scope, BlockExpression node, List<Expression> body, Node parent) {
        return new BlockExpression(node.blockId, node.definition, body);
    }

    @Override
    public Node apply(Processor processor, Scope scope, LetConstant constant, Expression body, Node parent) {
        return new LetConstant(constant.definition, constant.letName, constant.returnType, body);
    }

    @Override
    public Node apply(Processor processor, Scope scope, LetFunction node, Expression body, Node parent) {
        return new LetFunction(node.letId, node.definition, node.letName, node.parameters, node.returnType, body, node.isAsync, node.isClassLevel);
    }

    @Override
    public Expression apply(Processor processor, Scope scope, Lambda node, Expression body, Node parent) {
        return new Lambda(node.lambdaId, node.definition, node.parameters, body, node.isAsync, node.typeRepresentation);
    }

    @Override
    public Expression apply(Processor processor, Scope scope, IfElse node, Expression condition, Expression whenTrue, Expression whenFalse, Node parent) {
        return new IfElse(node.definition, condition, whenTrue, whenFalse);
    }

    @Override
    public Expression apply(Processor processor, Scope scope, Continuation node, Expression body, Node parent) {
        return new Continuation(node.definition, body, node.fanOut);
    }

    private void register(Scope scope, Contract contract) {
        Map<String, FunctionType> methods = new HashMap<>();
        Map<String, FunctionType> classLevelMethods = new HashMap<>();
        ContractType contractType = new ContractType(contract.definition, contract.name, contract.extensions, methods, classLevelMethods);

        try {
            scope.registerType(contract.name, contractType);
        } catch (TypeCanNotBeReassignedException e) {
            log.syntaxError(new SonataSyntaxError(contract, "Can not redefine a contract, however, contract '" + contract.name + "' has been already defined in: " + e.initialAssignment()));
        }

        contract.extensions.stream().map(e -> scope.resolveType(new BasicASTTypeRepresentation(null, e))).map(Optional::get).forEach(extension -> {
            methods.putAll(extension.methods());
        });

        contract.body.stream().map(a -> (LetFunction) a).filter(e -> !e.isClassLevel).forEach(let -> {
            Type returnType = scope.resolveType(let.returnType).orElse(Scope.TYPE_ANY);
            List<Type> parameters = let.parameters.stream().map(a -> Scope.TYPE_ANY).collect(Collectors.toList());

            methods.put(let.letName, new FunctionType(let.definition, let.letName, returnType, parameters));
        });

        contract.body.stream().map(a -> (LetFunction) a).filter(e -> e.isClassLevel).forEach(let -> {
            Type returnType = scope.resolveType(let.returnType).orElse(Scope.TYPE_ANY);
            List<Type> parameters = let.parameters.stream().map(a -> Scope.TYPE_ANY).collect(Collectors.toList());

            classLevelMethods.put(let.letName, new FunctionType(let.definition, let.letName, returnType, parameters));
        });
    }

    private boolean verify(Contract contract) {
        return contract.body.stream().allMatch(e -> {
            if (e instanceof LetFunction) {
                LetFunction letFunction = (LetFunction) e;
                if (!letFunction.isClassLevel && letFunction.body != null) {
                    log.syntaxError(new SonataSyntaxError(e, "Contracts only allow let function declarations, meaning let functions without body."));
                    return false;
                }
            } else {
                log.syntaxError(new SonataSyntaxError(e, "Contracts only allow let function declarations."));
                return false;
            }

            return true;
        });
    }
}
