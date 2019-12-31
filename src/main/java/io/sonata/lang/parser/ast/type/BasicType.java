package io.sonata.lang.parser.ast.type;

import io.sonata.lang.source.SourcePosition;

public class BasicType extends ComposedType implements Type {
    public final SourcePosition definition;
    public final String name;

    public BasicType(SourcePosition definition, String name) {
        this.definition = definition;
        this.name = name;
    }

    public static BasicType named(SourcePosition definition, String name) {
        return new BasicType(definition, name);
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
