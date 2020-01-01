package io.sonata.lang.analyzer.typeSystem;

import io.sonata.lang.analyzer.Processor;
import io.sonata.lang.log.CompilerLog;
import io.sonata.lang.analyzer.typeSystem.exception.TypeCanNotBeReassignedException;
import io.sonata.lang.exception.SonataSyntaxError;
import io.sonata.lang.parser.ast.Node;
import io.sonata.lang.parser.ast.ScriptNode;
import io.sonata.lang.parser.ast.classes.entities.EntityClass;
import io.sonata.lang.parser.ast.classes.values.ValueClass;

public final class ClassScopeProcessor implements Processor {
    private final CompilerLog log;
    private final Scope rootScope;

    public ClassScopeProcessor(CompilerLog log, Scope rootScope) {
        this.log = log;
        this.rootScope = rootScope;
    }

    @Override
    public Node apply(Node node) {
        if (node instanceof ScriptNode) {
            ((ScriptNode) node).nodes.forEach(this::apply);
        }

        if (node instanceof EntityClass) {
            final String className = ((EntityClass) node).name;
            try {
                rootScope.registerType(className, new EntityClassType(node.definition(), className));
            } catch (TypeCanNotBeReassignedException e) {
                log.syntaxError(new SonataSyntaxError(node, "Entity classes can not be redefined, but '" + className + "' is defined at least twice. Found on " + e.initialAssignment()));
            }
        }

        if (node instanceof ValueClass) {
            final String className = ((ValueClass) node).name;
            try {
                rootScope.registerType(className, new ValueClassType(node.definition(), className));
            } catch (TypeCanNotBeReassignedException e) {
                log.syntaxError(new SonataSyntaxError(node, "Value classes can not be redefined, but " + className + " is defined at least twice."));
            }
        }

        return node;
    }

    @Override
    public String phase() {
        return "CLASS PROCESSING";
    }
}
