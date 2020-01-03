package io.sonata.lang.parser.ast.exp;

import io.sonata.lang.source.SourcePosition;
import io.sonata.lang.tokenizer.token.Token;

import java.util.Collections;
import java.util.List;

import static io.sonata.lang.javaext.Lists.append;

public class PartialBlockExpression implements Expression {
    public final SourcePosition definition;
    public final List<Expression> nodes;
    public final Expression currentNode;

    private PartialBlockExpression(SourcePosition definition, List<Expression> nodes, Expression currentNode) {
        this.definition = definition;
        this.nodes = nodes;
        this.currentNode = currentNode;
    }

    public static PartialBlockExpression initial(SourcePosition definition) {
        return new PartialBlockExpression(definition, Collections.emptyList(), EmptyExpression.instance());
    }

    @Override
    public Expression consume(Token token) {
        Expression nextNode = currentNode.consume(token);
        if (nextNode == null) {
            if (token.representation().equals("}")) {
                if (currentNode instanceof EmptyExpression) {
                    return new BlockExpression(definition, nodes);
                }

                return new BlockExpression(definition, append(nodes, currentNode));
            }

            if (!(currentNode instanceof EmptyExpression)) {
                return new PartialBlockExpression(definition, append(nodes, currentNode), EmptyExpression.instance()).consume(token);
            }

            return this;
        }

        return new PartialBlockExpression(definition, nodes, nextNode);
    }

    @Override
    public String representation() {
        return null;
    }

    @Override
    public SourcePosition definition() {
        return definition;
    }
}
