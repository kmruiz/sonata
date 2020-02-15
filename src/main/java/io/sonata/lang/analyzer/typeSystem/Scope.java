/*
 * Copyright (c) 2020 Kevin Mas Ruiz
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package io.sonata.lang.analyzer.typeSystem;

import io.sonata.lang.analyzer.typeSystem.exception.TypeCanNotBeReassignedException;
import io.sonata.lang.exception.SonataSyntaxError;
import io.sonata.lang.log.CompilerLog;
import io.sonata.lang.parser.ast.Node;
import io.sonata.lang.parser.ast.Scoped;
import io.sonata.lang.parser.ast.type.ASTTypeRepresentation;
import io.sonata.lang.parser.ast.type.ArrayASTTypeRepresentation;
import io.sonata.lang.parser.ast.type.BasicASTTypeRepresentation;
import io.sonata.lang.parser.ast.type.FunctionASTTypeRepresentation;
import io.sonata.lang.source.Source;
import io.sonata.lang.source.SourcePosition;

import java.util.*;
import java.util.stream.Collectors;

public final class Scope {
    private static final SourcePosition INTERNAL = new SourcePosition(Source.fromLiteral("<internal>"), 1, 1);

    public static final ValueClassType TYPE_ANY = new ValueClassType(INTERNAL, "any", Collections.emptyMap(), Collections.emptyMap());
    public static final ValueClassType TYPE_RECORD = new ValueClassType(INTERNAL, "record", Collections.emptyMap(), Collections.emptyMap());
    public static final ValueClassType TYPE_BOOLEAN = new ValueClassType(INTERNAL, "boolean", Collections.emptyMap(), Collections.emptyMap());
    public static final ValueClassType TYPE_NUMBER = new ValueClassType(INTERNAL, "number", Collections.emptyMap(), Collections.emptyMap());
    public static final ValueClassType TYPE_STRING = new ValueClassType(INTERNAL, "string", Collections.emptyMap(), Collections.emptyMap());

    public static class Variable {
        public final Node definition;
        public final Type type;

        public Variable(Node definition, Type type) {
            this.definition = definition;
            this.type = type;
        }

        @Override
        public String toString() {
            return "Variable{" +
                    "definition=" + definition +
                    ", type=" + type +
                    '}';
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

        if (!foundScope.isPresent()) {
            Scope scope = new Scope(anchorRepresentation, this, new ArrayList<>(), new HashMap<>(), new HashMap<>());
            children.add(scope);
            return scope;
        }

        return foundScope.get();
    }

    public boolean isClassLoaded(String className) {
        return resolveType(new BasicASTTypeRepresentation(null, className)).isPresent();
    }

    public Optional<Type> resolveType(ASTTypeRepresentation astTypeRepresentation) {
        if (astTypeRepresentation instanceof FunctionASTTypeRepresentation) {
            FunctionASTTypeRepresentation fn = (FunctionASTTypeRepresentation) astTypeRepresentation;
            List<Type> paramTypes = fn.parameters.stream().map(this::resolveType).map(Optional::get).collect(Collectors.toList());

            return Optional.of(new FunctionType(fn.definition, "<anonymous>", resolveType(fn.returnASTTypeRepresentation).orElse(TYPE_ANY), paramTypes));
        }

        if (astTypeRepresentation instanceof ArrayASTTypeRepresentation) {
            ArrayASTTypeRepresentation arrayRepr = (ArrayASTTypeRepresentation) astTypeRepresentation;
            return Optional.of(new ArrayType(resolveType(arrayRepr.base).orElse(TYPE_ANY), arrayRepr.definition()));
        }

        if (astTypeRepresentation instanceof BasicASTTypeRepresentation) {
            BasicASTTypeRepresentation basicType = (BasicASTTypeRepresentation) astTypeRepresentation;
            Type typeIfAny = typeContext.get(basicType.name);
            if (typeIfAny == null) {
                return Optional.ofNullable(parent).flatMap(scope -> scope.resolveType(basicType));
            }

            return Optional.of(typeIfAny);
        }

        return Optional.empty();
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

    public void enrichVariable(String name, Node definition, Type type) {
        final Variable varInScope = variableContext.get(name);
        if (varInScope == null) {
            if (parent == null) {
                throw new IllegalStateException("Could not find variable " + name + " in scope " + anchor + ". Defined in " + definition.definition() + ": " + definition.representation());
            }
            parent.enrichVariable(name, definition, type);
            return;
        }

        variableContext.put(name, new Scope.Variable(definition, type));
    }

    public boolean inEntityClass() {
        if (this.anchor == null) {
            return false;
        }

        return this.anchor.startsWith("entity class") || parent.inEntityClass();
    }

    public void validateContractFulfillment(CompilerLog log) {
        typeContext.values().stream().filter(e -> e instanceof EntityClassType).map(e -> (EntityClassType) e).forEach(entityClass -> {
           entityClass.contracts.forEach(contract -> {
              contract.methods.values().forEach(contractMethod -> {
                  if (!entityClass.methods.containsKey(contractMethod.name)) {
                      log.syntaxError(new SonataSyntaxError(entityClass.definition, "To implement a contract, you must implement all methods defined. Missing method '" + contractMethod.name + "' definition in contract '" + contract.name + "'"));
                  }
              });
           });
        });
    }

    @Override
    public String toString() {
        return "Scope{" +
                "anchor='" + anchor + '\'' +
                ", children=" + children +
                ", typeContext=" + typeContext +
                ", variableContext=" + variableContext +
                '}';
    }
}
