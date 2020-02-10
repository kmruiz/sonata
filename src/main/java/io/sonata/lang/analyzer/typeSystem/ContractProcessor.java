/*
 * Copyright (c) 2020 Kevin Mas Ruiz
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package io.sonata.lang.analyzer.typeSystem;

import io.sonata.lang.analyzer.Processor;
import io.sonata.lang.analyzer.typeSystem.exception.TypeCanNotBeReassignedException;
import io.sonata.lang.exception.SonataSyntaxError;
import io.sonata.lang.log.CompilerLog;
import io.sonata.lang.parser.ast.Node;
import io.sonata.lang.parser.ast.ScriptNode;
import io.sonata.lang.parser.ast.classes.contracts.Contract;
import io.sonata.lang.parser.ast.let.LetFunction;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class ContractProcessor implements Processor {
    private final CompilerLog log;
    private final Scope scope;

    public ContractProcessor(CompilerLog log, Scope scope) {
        this.log = log;
        this.scope = scope;
    }

    @Override
    public Node apply(Node node) {
        if (node instanceof ScriptNode) {
            ScriptNode script = (ScriptNode) node;
            script.nodes.forEach(this::apply);
        }

        if (node instanceof Contract) {
            Contract contract = (Contract) node;
            if (verify(contract)) {
                register(contract);
            }
        }

        return node;
    }

    private void register(Contract contract) {
        Map<String, FunctionType> methods = contract.body.stream().map(a -> (LetFunction) a).filter(e -> !e.isClassLevel).map(let -> {
            Type returnType = scope.resolveType(let.returnType).orElse(Scope.TYPE_ANY);
            List<Type> parameters = let.parameters.stream().map(a -> Scope.TYPE_ANY).collect(Collectors.toList());

            return new FunctionType(let.definition, let.letName, returnType, parameters);
        }).collect(Collectors.toMap(k -> k.name, v -> v));

        Map<String, FunctionType> classLevelMethods = contract.body.stream().map(a -> (LetFunction) a).filter(e -> e.isClassLevel).map(let -> {
            Type returnType = scope.resolveType(let.returnType).orElse(Scope.TYPE_ANY);
            List<Type> parameters = let.parameters.stream().map(a -> Scope.TYPE_ANY).collect(Collectors.toList());

            return new FunctionType(let.definition, let.letName, returnType, parameters);
        }).collect(Collectors.toMap(k -> k.name, v -> v));

        ContractType contractType = new ContractType(contract.definition, contract.name, methods, classLevelMethods);
        try {
            scope.registerType(contract.name, contractType);
        } catch (TypeCanNotBeReassignedException e) {
            log.syntaxError(new SonataSyntaxError(contract, "Can not redefine a contract, however, contract '" + contract.name + "' has been already defined in: " + e.initialAssignment()));
        }
    }

    private boolean verify(Contract contract) {
        return contract.body.stream().allMatch(e -> {
            LetFunction letFunction = (LetFunction) e;
            if (e == null) {
                if (!letFunction.isClassLevel) {
                    log.syntaxError(new SonataSyntaxError(e, "Contracts only allow let function declarations."));
                    return false;
                }
            } else {
                if (letFunction.body != null && !letFunction.isClassLevel) {
                    log.syntaxError(new SonataSyntaxError(e, "Contracts only allow let function declarations, meaning let functions without body."));
                    return false;
                }
            }

            return true;
        });
    }

    @Override
    public String phase() {
        return "CONTRACTS";
    }
}
