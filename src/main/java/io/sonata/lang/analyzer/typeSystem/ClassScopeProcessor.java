package io.sonata.lang.analyzer.typeSystem;

import io.sonata.lang.analyzer.Processor;
import io.sonata.lang.analyzer.log.CompilerLog;
import io.sonata.lang.analyzer.typeSystem.exception.TypeCanNotBeReassignedException;
import io.sonata.lang.exception.SonataSyntaxErrorException;
import io.sonata.lang.parser.ast.Node;
import io.sonata.lang.parser.ast.ScriptNode;
import io.sonata.lang.parser.ast.classes.entities.EntityClass;

public class ClassScopeProcessor implements Processor {
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
                rootScope.register(className, new EntityClassType(node.definition(), className));
                CompilerLog.console().inPhase("PROCESSING CLASS " + ((EntityClass) node).name);
            } catch (TypeCanNotBeReassignedException e) {
                log.syntaxError(new SonataSyntaxErrorException(node, "Entity classes can not be redefined."));
            }
        }

        return node;
    }

    @Override
    public String phase() {
        return "CLASS PROCESSING";
    }
}
