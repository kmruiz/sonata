package io.sonata.lang.parser.ast.let;

import io.sonata.lang.parser.ast.exp.Expression;
import io.sonata.lang.parser.ast.type.ASTType;
import io.sonata.lang.source.SourcePosition;
import io.sonata.lang.tokenizer.token.Token;

public class LetConstant implements Expression {
    public final SourcePosition definition;
    public final String letName;
    public final ASTType returnType;
    public final Expression body;

    public LetConstant(SourcePosition definition, String letName, ASTType returnType, Expression body) {
        this.definition = definition;
        this.letName = letName;
        this.returnType = returnType;
        this.body = body;
    }

    @Override
    public String representation() {
        return "let " + letName + ":" + (returnType != null ? returnType.representation() : "?") + " = " + body.representation();
    }

    @Override
    public Expression consume(Token token) {
        return null;
    }

    @Override
    public SourcePosition definition() {
        return definition;
    }
}
