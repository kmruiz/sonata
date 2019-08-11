package io.sonata.lang.parser.ast.exp;

import io.sonata.lang.parser.ast.Node;
import io.sonata.lang.tokenizer.token.Token;

import java.util.List;
import java.util.stream.Collectors;

public class BlockExpression implements Expression {
    public final List<Node> expressions;

    public BlockExpression(List<Node> expressions) {
        this.expressions = expressions;
    }

    @Override
    public Expression consume(Token token) {
        return null;
    }

    @Override
    public String representation() {
        return "{" + expressions.stream().map(Node::representation).collect(Collectors.joining("\n")) + "}";
    }
}
