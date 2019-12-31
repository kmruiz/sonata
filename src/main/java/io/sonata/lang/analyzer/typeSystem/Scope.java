package io.sonata.lang.analyzer.typeSystem;

import io.sonata.lang.parser.ast.Node;

import java.util.List;
import java.util.Map;

public final class Scope {
    private final Node anchor;
    private final Scope parent;
    private final List<Scope> children;
    private final Map<String, Type> context;

    public Scope(Node anchor, Scope parent, List<Scope> children, Map<String, Type> context) {
        this.anchor = anchor;
        this.parent = parent;
        this.children = children;
        this.context = context;
    }
}
