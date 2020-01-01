package io.sonata.lang.parser.ast.let;

import io.sonata.lang.parser.ast.exp.Expression;
import io.sonata.lang.parser.ast.let.fn.Parameter;
import io.sonata.lang.parser.ast.type.ASTType;
import io.sonata.lang.source.SourcePosition;
import io.sonata.lang.tokenizer.token.Token;

import java.util.List;
import java.util.stream.Collectors;

public class LetFunction implements Expression {
    public final SourcePosition definition;
    public final String letName;
    public final List<Parameter> parameters;
    public final ASTType returnASTType;
    public final Expression body;

    public LetFunction(SourcePosition definition, String letName, List<Parameter> parameters, ASTType returnASTType, Expression body) {
        this.definition = definition;
        this.letName = letName;
        this.parameters = parameters;
        this.returnASTType = returnASTType;
        this.body = body;
    }

    @Override
    public String representation() {
        return "let " + letName + "(" + parameters.stream().map(Parameter::representation).collect(Collectors.joining(", ")) + "): " + returnASTType.representation() + " = " + body.representation();
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
