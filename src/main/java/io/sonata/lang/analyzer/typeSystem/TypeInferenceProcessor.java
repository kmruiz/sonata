/*
 * Copyright (c) 2020 Kevin Mas Ruiz
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package io.sonata.lang.analyzer.typeSystem;

import io.sonata.lang.analyzer.Processor;
import io.sonata.lang.log.CompilerLog;
import io.sonata.lang.parser.ast.Node;
import io.sonata.lang.parser.ast.ScriptNode;
import io.sonata.lang.parser.ast.classes.entities.EntityClass;
import io.sonata.lang.parser.ast.classes.values.ValueClass;
import io.sonata.lang.parser.ast.exp.*;
import io.sonata.lang.parser.ast.let.LetConstant;
import io.sonata.lang.parser.ast.let.LetFunction;
import io.sonata.lang.parser.ast.type.ASTType;
import io.sonata.lang.parser.ast.type.BasicASTType;
import io.sonata.lang.parser.ast.type.EmptyASTType;
import io.sonata.lang.parser.ast.type.FunctionASTType;

import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

public final class TypeInferenceProcessor implements Processor {
    private final CompilerLog log;
    private final Scope scope;

    public TypeInferenceProcessor(CompilerLog log, Scope scope) {
        this.log = log;
        this.scope = scope;
    }

    @Override
    public Node apply(Node node) {
        if (node instanceof ScriptNode) {
            ScriptNode script = (ScriptNode) node;
            List<Node> nodes = script.nodes.stream().map(this::apply).collect(toList());
            return new ScriptNode(script.log, nodes, script.currentNode, script.requiresNotifier);
        }

        if (node instanceof EntityClass) {
            EntityClass entityClass = (EntityClass) node;
            List<Node> body = entityClass.body.stream().map(this::apply).collect(toList());
            return new EntityClass(entityClass.definition, entityClass.name, entityClass.definedFields, body);
        }

        if (node instanceof ValueClass) {
            ValueClass valueClass  = (ValueClass) node;
            List<Node> body = valueClass.body.stream().map(this::apply).collect(toList());
            return new ValueClass(valueClass.definition, valueClass.name, valueClass.definedFields, body);
        }

        if (node instanceof LetConstant) {
            LetConstant constant = (LetConstant) node;
            ASTType type = constant.returnType;
            if (type == null || type instanceof EmptyASTType) {
                type = infer(constant.body);
            }

            return new LetConstant(constant.definition, constant.letName, type, (Expression) apply(constant.body));
        }

        if (node instanceof LetFunction) {
            LetFunction fn = (LetFunction) node;
            ASTType type = fn.returnType;
            if (type == null || type instanceof EmptyASTType) {
                type = infer(fn.body);
            }

            return new LetFunction(fn.letId, fn.definition, fn.letName, fn.parameters, type, (Expression) apply(fn.body));
        }

        if (node instanceof BlockExpression) {
            BlockExpression blockExpression = (BlockExpression) node;
            List<Expression> expressions = blockExpression.expressions.stream().map(this::apply).map(t -> (Expression) t).collect(toList());
            return new BlockExpression(blockExpression.blockId, blockExpression.definition, expressions);
        }

        if (node instanceof FunctionCall) {
            FunctionCall fc = (FunctionCall) node;
            List<Expression> parameters = fc.arguments.stream().map(this::apply).map(e -> (Expression) e).collect(toList());

            if (fc.receiver instanceof Atom) {
                Atom name = (Atom) fc.receiver;
                final Optional<Type> type = scope.resolveType(name.value);
                if (type.isPresent()) {
                    return new FunctionCall(fc.receiver, parameters, new BasicASTType(type.get().definition(), type.get().name()));
                }

                final Optional<Scope.Variable> maybeVariable = scope.resolveVariable(name.value);
                if (maybeVariable.isPresent()) {
                    Scope.Variable variable = maybeVariable.get();
                    if (variable.type instanceof FunctionType) {
                        FunctionType fnType = (FunctionType) variable.type;
                        return new FunctionCall(fc.receiver, parameters, new BasicASTType(fnType.returnType.definition(), fnType.returnType.name()));
                    }
                }
            }

            return new FunctionCall(fc.receiver, parameters, fc.expressionType);
        }

        if (node instanceof IfElse) {
            IfElse ifElse = (IfElse) node;

            return new IfElse(
                    ifElse.definition,
                    (Expression) apply(ifElse.condition),
                    (Expression) apply(ifElse.whenTrue),
                    ifElse.whenFalse != null ? (Expression) apply(ifElse.whenFalse) : null
            );
        }

        if (node instanceof SimpleExpression) {
            SimpleExpression expr = (SimpleExpression) node;
            return new SimpleExpression((Expression) apply(expr.leftSide), expr.operator, (Expression) apply(expr.rightSide));
        }

        return node;
    }

    public ASTType infer(Expression expression) {
        if (expression == null) {
            return new BasicASTType(null, "any");
        }

        if (expression.type() != null && !(expression.type() instanceof EmptyASTType) && !expression.type().representation().equals("any")) {
            return expression.type();
        }

        if (expression instanceof Atom) {
            switch (((Atom) expression).type) {
                case NUMERIC:
                    return new BasicASTType(expression.definition(), "number");
                case STRING:
                    return new BasicASTType(expression.definition(), "string");
                case BOOLEAN:
                    return new BasicASTType(expression.definition(), "boolean");
                case NULL:
                    return new BasicASTType(expression.definition(), "null");
            }
        }

        if (expression instanceof Lambda) {
            Lambda lambda = (Lambda) expression;
            return new FunctionASTType(lambda.definition, lambda.parameters.stream().map(e -> e.astType).collect(toList()), infer(((Lambda) expression).body));
        }

        return new BasicASTType(expression.definition(), "any");
    }

    @Override
    public String phase() {
        return "TYPE INFERENCE";
    }
}
