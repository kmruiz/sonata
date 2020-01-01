package io.sonata.lang.parser.ast.type;

import io.sonata.lang.source.SourcePosition;

import java.util.List;
import java.util.stream.Collectors;

public class FunctionASTType extends ComposedASTType implements ASTType {
    public final SourcePosition definition;
    public final List<ASTType> parameters;
    public final ASTType returnASTType;

    public FunctionASTType(SourcePosition definition, List<ASTType> parameters, ASTType returnASTType) {
        this.definition = definition;
        this.parameters = parameters;
        this.returnASTType = returnASTType;
    }

    @Override
    public String representation() {
        return "let(" + parameters.stream().map(Object::toString).collect(Collectors.joining(",")) + "): " + returnASTType.toString();
    }

    @Override
    public SourcePosition definition() {
        return definition;
    }
}
