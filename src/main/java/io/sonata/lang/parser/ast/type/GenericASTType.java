package io.sonata.lang.parser.ast.type;

import io.sonata.lang.source.SourcePosition;

import java.util.List;
import java.util.stream.Collectors;

public class GenericASTType extends ComposedASTType implements ASTType {
    public final ASTType base;
    public final List<ASTType> parameters;

    public GenericASTType(ASTType base, List<ASTType> parameters) {
        this.base = base;
        this.parameters = parameters;
    }

    @Override
    public String representation() {
        return base.representation() + "[" + parameters.stream().map(ASTType::representation).collect(Collectors.joining(", ")) + "]";
    }

    @Override
    public SourcePosition definition() {
        return base.definition();
    }
}
