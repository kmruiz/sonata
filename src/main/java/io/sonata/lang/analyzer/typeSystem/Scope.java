package io.sonata.lang.analyzer.typeSystem;

import io.sonata.lang.analyzer.typeSystem.exception.TypeCanNotBeReassignedException;
import io.sonata.lang.parser.ast.Node;

import java.util.*;

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

    public static Scope root() {
        Scope root = new Scope(null, null, new ArrayList<>(), new HashMap<>());
        try {
            root.register("string", new ValueClassType(null, "string"));
            root.register("number", new ValueClassType(null, "number"));
        } catch (TypeCanNotBeReassignedException e) {
            throw new IllegalStateException(e);
        }

        return root;
    }

    public Scope diveIn(Node anchor) {
        Scope scope = new Scope(anchor, this, new ArrayList<>(), new HashMap<>());
        children.add(scope);

        return scope;
    }

    public Optional<Type> resolve(String name) {
        final Type typeInThisScope = context.get(name);
        if (typeInThisScope == null) {
            if (parent != null) {
                return parent.resolve(name);
            }

            return Optional.empty();
        }

        return Optional.of(typeInThisScope);
    }

    public void register(String name, Type type) throws TypeCanNotBeReassignedException {
        final Type typeInThisScope = context.get(name);
        if (typeInThisScope == null) {
            context.put(name, type);
            return;
        }

        if (typeInThisScope.canBeReassigned()) {
            context.put(name, type);
            return;
        }

        throw new TypeCanNotBeReassignedException();
    }
}
