package io.sonata.lang.parser.ast.exp;

import io.sonata.lang.tokenizer.token.Token;

import java.util.Collections;
import java.util.List;

import static io.sonata.lang.javaext.Lists.append;

public class PartialBlockExpression implements Expression {
    public final List<Expression> nodes;
    public final Expression currentNode;

    private PartialBlockExpression(List<Expression> nodes, Expression currentNode) {
        this.nodes = nodes;
        this.currentNode = currentNode;
    }

    public static PartialBlockExpression initial() {
        return new PartialBlockExpression(Collections.emptyList(), EmptyExpression.instance());
    }

    @Override
    public Expression consume(Token token) {
        Expression nextNode = currentNode.consume(token);
        if (nextNode == null) {
            if (token.representation().equals("}")) {
                return new BlockExpression(append(nodes, currentNode));
            }

            return new PartialBlockExpression(append(nodes, currentNode), EmptyExpression.instance());
        }

        return new PartialBlockExpression(nodes, nextNode);
    }

    @Override
    public String representation() {
        return null;
    }
}
