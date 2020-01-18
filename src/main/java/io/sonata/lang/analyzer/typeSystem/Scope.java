/*
 * Copyright (c) 2020 Kevin Mas Ruiz
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package io.sonata.lang.analyzer.typeSystem;

import io.sonata.lang.analyzer.typeSystem.exception.TypeCanNotBeReassignedException;
import io.sonata.lang.parser.ast.Node;
import io.sonata.lang.parser.ast.Scoped;
import io.sonata.lang.parser.ast.type.ASTType;
import io.sonata.lang.parser.ast.type.BasicASTType;
import io.sonata.lang.parser.ast.type.FunctionASTType;

import java.util.*;
import java.util.stream.Collectors;

public final class Scope {
    public static final ValueClassType TYPE_ANY = new ValueClassType(null, "any", Collections.emptyMap(), Collections.emptyMap());
    public static final ValueClassType TYPE_RECORD = new ValueClassType(null, "record", Collections.emptyMap(), Collections.emptyMap());
    public static final ValueClassType TYPE_BOOLEAN = new ValueClassType(null, "boolean", Collections.emptyMap(), Collections.emptyMap());
    public static final ValueClassType TYPE_NUMBER = new ValueClassType(null, "number", Collections.emptyMap(), Collections.emptyMap());
    public static final ValueClassType TYPE_STRING = new ValueClassType(null, "string", Collections.emptyMap(), Collections.emptyMap());

    public static class Variable {
        public final Node definition;
        public final Type type;

        public Variable(Node definition, Type type) {
            this.definition = definition;
            this.type = type;
        }
    }

    private final String anchor;
    private final Scope parent;
    private final List<Scope> children;
    private final Map<String, Type> typeContext;
    private final Map<String, Variable> variableContext;

    public Scope(String anchor, Scope parent, List<Scope> children, Map<String, Type> typeContext, Map<String, Variable> variableContext) {
        this.anchor = anchor;
        this.parent = parent;
        this.children = children;
        this.typeContext = typeContext;
        this.variableContext = variableContext;
    }

    public static Scope root() {
        Scope root = new Scope(null, null, new ArrayList<>(), new HashMap<>(), new HashMap<>());
        try {
            root.registerType("string", TYPE_STRING);
            root.registerType("number", TYPE_NUMBER);
            root.registerType("boolean", TYPE_BOOLEAN);
            root.registerType("record", TYPE_RECORD);
            root.registerType("any", TYPE_ANY);
        } catch (TypeCanNotBeReassignedException e) {
            throw new IllegalStateException(e);
        }

        return root;
    }

    public Scope diveIn(Scoped anchor) {
        final String anchorRepresentation = anchor.scopeId();
        final Optional<Scope> foundScope = children.stream().filter(e -> e.anchor.equals(anchorRepresentation)).findFirst();

        if (foundScope.isPresent()) {
            return foundScope.get();
        }

        Scope scope = new Scope(anchorRepresentation, this, new ArrayList<>(), new HashMap<>(), new HashMap<>());
        children.add(scope);
        return scope;
    }

    public Scope diveInIfNeeded(Node node) {
        if (!(node instanceof Scoped)) {
            return this;
        }

        Scoped anchor = (Scoped) node;

        final String anchorRepresentation = anchor.scopeId();
        final Optional<Scope> foundScope = children.stream().filter(e -> e.anchor.equals(anchorRepresentation)).findFirst();

        return foundScope.orElse(this);
    }

    public Optional<Type> resolveType(ASTType astType) {
        if (astType instanceof BasicASTType) {
            return resolveType(astType.representation());
        }

        if (astType instanceof FunctionASTType) {
            FunctionASTType fn = (FunctionASTType) astType;
            List<Type> paramTypes = fn.parameters.stream().map(this::resolveType).map(Optional::get).collect(Collectors.toList());

            return Optional.of(new FunctionType(fn.definition, "<anonymous>", resolveType(fn.returnASTType).orElse(TYPE_ANY), paramTypes));
        }

        return Optional.empty();
    }

    public Optional<Type> resolveType(String name) {
        if (name.endsWith("[]")) {
            return resolveType(name.substring(0, name.length() - 2));
        }

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

    public void enrichType(String name, Type type) {
        final Type typeInThisScope = typeContext.get(name);
        if (typeInThisScope == null) {
            parent.enrichType(name, type);
            return;
        }

        typeContext.put(name, type);
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

    public boolean inEntityClass() {
        if (this.anchor == null) {
            return false;
        }

        return this.anchor.startsWith("entity class") || parent.inEntityClass();
    }
}
