package io.sonata.lang.analyzer.typeSystem;

import io.sonata.lang.analyzer.typeSystem.exception.TypeCanNotBeReassignedException;
import io.sonata.lang.parser.ast.Node;

import java.util.*;

public final class Scope {
    private final Node anchor;
    private final Scope parent;
    private final List<Scope> children;
    private final Map<String, Type> typeContext;
    private final Map<String, Type> variableContext;

    public Scope(Node anchor, Scope parent, List<Scope> children, Map<String, Type> typeContext, Map<String, Type> variableContext) {
        this.anchor = anchor;
        this.parent = parent;
        this.children = children;
        this.typeContext = typeContext;
        this.variableContext = variableContext;
    }

    public static Scope root() {
        Scope root = new Scope(null, null, new ArrayList<>(), new HashMap<>(), new HashMap<>());
        try {
            root.registerType("string", new ValueClassType(null, "string"));
            root.registerType("number", new ValueClassType(null, "number"));
        } catch (TypeCanNotBeReassignedException e) {
            throw new IllegalStateException(e);
        }

        return root;
    }

    public Scope diveIn(Node anchor) {
        Scope scope = new Scope(anchor, this, new ArrayList<>(), new HashMap<>(), new HashMap<>());
        children.add(scope);

        return scope;
    }

    public Optional<Type> resolveType(String name) {
        final Type typeInThisScope = typeContext.get(name);
        if (typeInThisScope == null) {
            if (parent != null) {
                return parent.resolveType(name);
            }

            return Optional.empty();
        }

        return Optional.of(typeInThisScope);
    }

    public Optional<Type> resolveVariable(String name) {
        final Type typeInThisScope = variableContext.get(name);
        if (typeInThisScope == null) {
            if (parent != null) {
                return parent.resolveVariable(name);
            }

            return Optional.empty();
        }

        return Optional.of(typeInThisScope);
    }

    public void registerType(String name, Type type) throws TypeCanNotBeReassignedException {
        final Type typeInThisScope = typeContext.get(name);
        if (typeInThisScope == null) {
            typeContext.put(name, type);
            return;
        }

        if (typeInThisScope.canBeReassigned()) {
            typeContext.put(name, type);
            return;
        }

        throw new TypeCanNotBeReassignedException();
    }

    public void registerVariable(String name, Type type) throws TypeCanNotBeReassignedException {
        final Type typeInThisScope = variableContext.get(name);
        if (typeInThisScope == null) {
            variableContext.put(name, type);
            return;
        }

        if (typeInThisScope.canBeReassigned()) {
            variableContext.put(name, type);
            return;
        }

        throw new TypeCanNotBeReassignedException();
    }
}
