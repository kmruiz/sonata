/*
 * Copyright (c) 2020 Kevin Mas Ruiz
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package io.sonata.lang.analyzer.fops;

import io.sonata.lang.analyzer.Processor;
import io.sonata.lang.analyzer.ProcessorIterator;
import io.sonata.lang.analyzer.ProcessorWrapper;
import io.sonata.lang.analyzer.typeSystem.ASTTypeReference;
import io.sonata.lang.analyzer.typeSystem.FunctionType;
import io.sonata.lang.analyzer.typeSystem.Scope;
import io.sonata.lang.analyzer.typeSystem.Type;
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
import io.sonata.lang.parser.ast.let.fn.SimpleParameter;
import io.sonata.lang.parser.ast.requires.RequiresNode;
import io.sonata.lang.parser.ast.type.ASTTypeRepresentation;

import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

public final class FunctionCompositionProcessor implements ProcessorIterator {
    private final CompilerLog log;

    public static Processor processorInstance(Scope scope, CompilerLog log) {
        return new ProcessorWrapper(scope, "FUNCTION COMPOSITION",
                new FunctionCompositionProcessor(log)
        );
    }

    private FunctionCompositionProcessor(CompilerLog log) {
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
        return composeIfNeeded(scope, new SimpleExpression(left, node.operator, right));
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

    private Expression composeIfNeeded(Scope scope, SimpleExpression expression) {
        if (!expression.operator.equals("->")) {
            return expression;
        }

        if (isFunction(scope, expression.leftSide) && isFunction(scope, expression.rightSide)) {
            List<SimpleParameter> newFnParams = parametersOf(scope, expression.leftSide);
            return compositionOf(newFnParams, expression.leftSide, expression.rightSide);
        }

        log.syntaxError(new SonataSyntaxError(expression, "Only functions can be composed."));
        return expression;
    }

    private boolean isFunction(Scope scope, Expression expr) {
        if (expr instanceof Lambda) {
            return true;
        }

        if (expr instanceof Atom) {
            ASTTypeReference typeRef = (ASTTypeReference) ((Atom) expr).type;
            return typeRef.type instanceof FunctionType;
        }

        if (expr instanceof SimpleExpression) {
            SimpleExpression sexpr = (SimpleExpression) expr;
            return isFunction(scope, sexpr.leftSide) && isFunction(scope, sexpr.rightSide) && sexpr.operator.equals("->");
        }

        return false;
    }

    private List<SimpleParameter> parametersOf(Scope scope, Expression expr) {
        ASTTypeRepresentation typeRepr = expr.type();
        if (typeRepr instanceof ASTTypeReference) {
            Type type = ((ASTTypeReference) typeRepr).type;
            if (type instanceof FunctionType) {
                return ((FunctionType) type).namedParameters();
            }
        }

        return emptyList();
    }

    private Expression compositionOf(List<SimpleParameter> params, Expression a, Expression b) {
        return Lambda.synthetic(a.definition(), params,
                new FunctionCall(b, singletonList(
                        new FunctionCall(a,
                                params.stream().map(p -> new Atom(p.definition, p.name, p.type)).collect(toList())
                        )
                ))
        );
    }
}
