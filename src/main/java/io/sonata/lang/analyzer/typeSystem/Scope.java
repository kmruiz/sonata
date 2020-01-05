package io.sonata.lang.analyzer.typeSystem;

import io.sonata.lang.analyzer.typeSystem.exception.TypeCanNotBeReassignedException;
import io.sonata.lang.parser.ast.Node;

import java.util.*;

public final class Scope {
    public static class Variable {
        public final Node definition;
        public final Type type;

        public Variable(Node definition, Type type) {
            this.definition = definition;
            this.type = type;
        }
    }

    private final Node anchor;
    private final Scope parent;
    private final List<Scope> children;
    private final Map<String, Type> typeContext;
    private final Map<String, Variable> variableContext;

    public Scope(Node anchor, Scope parent, List<Scope> children, Map<String, Type> typeContext, Map<String, Variable> variableContext) {
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
            root.registerType("boolean", new ValueClassType(null, "boolean"));
            root.registerType("record", new ValueClassType(null, "boolean"));
            root.registerType("null", new ValueClassType(null, "null"));
            root.registerType("any", new ValueClassType(null, "any"));
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

    public Optional<Variable> resolveVariable(String name) {
        final Variable typeInThisScope = variableContext.get(name);
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

        throw new TypeCanNotBeReassignedException(typeInThisScope.definition());
    }

    public void registerVariable(String name, Node definition, Type type) throws TypeCanNotBeReassignedException {
        final Variable variableInThisScope = variableContext.get(name);
        if (variableInThisScope == null) {
            variableContext.put(name, new Variable(definition, type));
            return;
        }

        if (variableInThisScope.type.canBeReassigned()) {
            variableContext.put(name, new Variable(definition, type));
            return;
        }

        throw new TypeCanNotBeReassignedException(variableInThisScope.definition.definition());
    }
}
