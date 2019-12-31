package io.sonata.lang.analyzer.typeSystem;

import io.sonata.lang.analyzer.Processor;
import io.sonata.lang.parser.ast.Node;
import io.sonata.lang.parser.ast.ScriptNode;
import io.sonata.lang.parser.ast.classes.entities.EntityClass;

public class ClassScopeProcessor implements Processor {
    private final Scope rootScope;

    public ClassScopeProcessor(Scope rootScope) {
        this.rootScope = rootScope;
    }

    @Override
    public Node apply(Node node) {
        if (node instanceof ScriptNode) {
            ((ScriptNode) node).nodes.forEach(this::apply);
        }

        if (node instanceof EntityClass) {
            final String className = ((EntityClass) node).name;
//            try {
////                rootScope.register(className, new EntityClassType(className));
//            } catch (TypeCanNotBeReassignedException e) {
////                throw new S
//            }
        }
        return node;
    }

    @Override
    public String phase() {
        return "CLASS PROCESSING";
    }
}
