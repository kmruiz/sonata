/*
 * Copyright (c) 2020 Kevin Mas Ruiz
 *
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
import io.sonata.lang.parser.ast.exp.Atom;
import io.sonata.lang.parser.ast.exp.BlockExpression;
import io.sonata.lang.parser.ast.exp.Expression;
import io.sonata.lang.parser.ast.let.LetConstant;
import io.sonata.lang.parser.ast.let.LetFunction;
import io.sonata.lang.parser.ast.type.ASTType;
import io.sonata.lang.parser.ast.type.BasicASTType;
import io.sonata.lang.parser.ast.type.EmptyASTType;

import java.util.List;
import java.util.stream.Collectors;

public class TypeInferenceProcessor implements Processor {
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
            List<Node> nodes = script.nodes.stream().map(this::apply).collect(Collectors.toList());
            return new ScriptNode(script.log, nodes, script.currentNode, script.requiresNotifier);
        }

        if (node instanceof EntityClass) {
            EntityClass entityClass = (EntityClass) node;
            List<Node> body = entityClass.body.stream().map(this::apply).collect(Collectors.toList());
            return new EntityClass(entityClass.definition, entityClass.name, entityClass.definedFields, body);
        }

        if (node instanceof ValueClass) {
            ValueClass valueClass  = (ValueClass) node;
            List<Node> body = valueClass.body.stream().map(this::apply).collect(Collectors.toList());
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

            return new LetFunction(fn.definition, fn.letName, fn.parameters, type, (Expression) apply(fn.body));
        }

        if (node instanceof BlockExpression) {
            BlockExpression blockExpression = (BlockExpression) node;
            List<Expression> expressions = blockExpression.expressions.stream().map(this::apply).map(t -> (Expression) t).collect(Collectors.toList());
            return new BlockExpression(blockExpression.definition, expressions);
        }

        return node;
    }

    public ASTType infer(Expression expression) {
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

        if (expression == null) {
            return new BasicASTType(null, "any");
        }

        return new BasicASTType(expression.definition(), "any");
    }

    @Override
    public String phase() {
        return "TYPE INFERENCE";
    }
}
