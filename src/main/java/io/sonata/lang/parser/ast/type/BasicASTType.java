package io.sonata.lang.parser.ast.type;

import io.sonata.lang.source.SourcePosition;

public class BasicASTType extends ComposedASTType implements ASTType {
    public final SourcePosition definition;
    public final String name;

    public BasicASTType(SourcePosition definition, String name) {
        this.definition = definition;
        this.name = name;
    }

    public static BasicASTType named(SourcePosition definition, String name) {
        return new BasicASTType(definition, name);
    }

    @Override
    public String representation() {
        return name;
    }

    @Override
    public SourcePosition definition() {
        return definition;
    }
}
