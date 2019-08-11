package io.sonata.lang.parser.ast.let;

import io.sonata.lang.parser.ast.Node;
import io.sonata.lang.parser.ast.exp.Expression;
import io.sonata.lang.parser.ast.type.Type;
import io.sonata.lang.tokenizer.token.Token;

public class LetConstant implements Node {
    public final String letName;
    public final Type returnType;
    public final Expression body;

    public LetConstant(String letName, Type returnType, Expression body) {
        this.letName = letName;
        this.returnType = returnType;
        this.body = body;
    }

    @Override
    public String representation() {
        return "let " + letName + ":" + (returnType != null ? returnType.representation() : "?") + " = " + body.representation();
    }

    @Override
    public Node consume(Token token) {
        return null;
    }
}
