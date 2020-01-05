/*
 * Copyright (c) 2020 Kevin Mas Ruiz
 *
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
import io.sonata.lang.parser.ast.classes.entities.EntityClass;
import io.sonata.lang.parser.ast.classes.values.ValueClass;
import io.sonata.lang.parser.ast.exp.BlockExpression;
import io.sonata.lang.parser.ast.let.LetConstant;
import io.sonata.lang.parser.ast.let.LetFunction;
import io.sonata.lang.parser.ast.let.fn.SimpleParameter;

public final class LetVariableProcessor implements Processor {
    private final CompilerLog log;
    private final Scope scope;

    public LetVariableProcessor(CompilerLog log, Scope scope) {
        this.log = log;
        this.scope = scope;
    }

    @Override
    public Node apply(Node node) {
        apply(scope, node);

        return node;
    }

    private void apply(Scope scope, Node node) {
        if (node instanceof ScriptNode) {
            Scope scriptScope = scope.diveIn(node);
            ((ScriptNode) node).nodes.forEach(child -> apply(scriptScope, child));
        }

        if (node instanceof EntityClass) {
            Scope classScope = scope.diveIn(node);
            ((EntityClass) node).definedFields.forEach(field -> apply(classScope, field));
            ((EntityClass) node).body.forEach(childDef -> apply(classScope, childDef));
        }

        if (node instanceof ValueClass) {
            Scope classScope = scope.diveIn(node);
            ((ValueClass) node).definedFields.forEach(field -> apply(classScope, field));
            ((ValueClass) node).body.forEach(childDef -> apply(classScope, childDef));
        }

        if (node instanceof BlockExpression) {
            Scope blockScope = scope.diveIn(node);
            ((BlockExpression) node).expressions.forEach(expr -> apply(blockScope, expr));
        }

        if (node instanceof LetConstant) {
            String letName = ((LetConstant) node).letName;
            try {
                scope.registerVariable(letName, node, scope.resolveType(((LetConstant) node).returnType.representation()).orElse(new ValueClassType(null, "any")));
                apply(scope, ((LetConstant) node).body);
            } catch (TypeCanNotBeReassignedException e) {
                log.syntaxError(new SonataSyntaxError(node, "Variable '" + letName + "' has been already defined. Found on " + e.initialAssignment()));
            }
        }

        if (node instanceof LetFunction) {
            String letName = ((LetFunction) node).letName;
            try {
                scope.registerVariable(letName, node, scope.resolveType(((LetFunction) node).returnType.representation()).orElse(new ValueClassType(null, "any")));
            } catch (TypeCanNotBeReassignedException e) {
                // It's fine, let functions can be overloaded
            }

            Scope letScope = scope.diveIn(node);
            ((LetFunction) node).parameters.stream().filter(e -> e instanceof SimpleParameter).forEach(parameter -> {
                String paramName = ((SimpleParameter) parameter).name;
                try {
                    letScope.registerVariable(paramName, parameter, scope.resolveType(((SimpleParameter) parameter).astType.representation()).orElse(new ValueClassType(null, "any")));
                } catch (TypeCanNotBeReassignedException e) {
                    log.syntaxError(new SonataSyntaxError(node, "Parameter '" + paramName + "' has been already defined. Found on " + e.initialAssignment()));
                }
            });
            apply(letScope, ((LetFunction) node).body);
        }
    }

    @Override
    public String phase() {
        return "LET PROCESSING";
    }
}
