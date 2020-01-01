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
            return new ScriptNode(nodes, script.currentNode, script.requiresNotifier);
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
            if (constant.returnType == null || constant.returnType instanceof EmptyASTType) {
                return new LetConstant(constant.definition, constant.letName, infer(constant.body), (Expression) apply(constant.body));
            }
        }

        if (node instanceof LetFunction) {
            LetFunction fn = (LetFunction) node;
            if (fn.returnType == null  || fn.returnType instanceof EmptyASTType) {
                return new LetFunction(fn.definition, fn.letName, fn.parameters, infer(fn.body), (Expression) apply(fn.body));
            }
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
                case IDENTIFIER:
                    switch (expression.representation()) {
                        case "true":
                        case "false":
                            return new BasicASTType(expression.definition(), "boolean");
                        case "null":
                            return new BasicASTType(expression.definition(), "null");
                    }
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
