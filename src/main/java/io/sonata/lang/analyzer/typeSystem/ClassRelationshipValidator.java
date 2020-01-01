package io.sonata.lang.analyzer.typeSystem;

import io.sonata.lang.analyzer.Processor;
import io.sonata.lang.exception.SonataSyntaxError;
import io.sonata.lang.log.CompilerLog;
import io.sonata.lang.parser.ast.Node;
import io.sonata.lang.parser.ast.ScriptNode;
import io.sonata.lang.parser.ast.classes.fields.Field;
import io.sonata.lang.parser.ast.classes.fields.SimpleField;
import io.sonata.lang.parser.ast.classes.values.ValueClass;
import io.sonata.lang.parser.ast.type.BasicASTType;
import io.sonata.lang.parser.ast.type.ASTType;

import java.util.Optional;

public final class ClassRelationshipValidator implements Processor {
    private final Scope scope;
    private final CompilerLog log;

    public ClassRelationshipValidator(CompilerLog log, Scope scope) {
        this.scope = scope;
        this.log = log;
    }

    @Override
    public String phase() {
        return "CLASS RELATIONSHIP VALIDATION";
    }

    @Override
    public Node apply(Node node) {
        if (node instanceof ScriptNode) {
            ((ScriptNode) node).nodes.forEach(this::apply);
        }

        if (node instanceof ValueClass) {
            ValueClass vc = (ValueClass) node;
            vc.definedFields.forEach(field -> this.validateIsNotAnEntity(vc, field));
        }

        return node;
    }

    private void validateIsNotAnEntity(ValueClass valueClass, Field field) {
        ASTType fieldASTType = ((SimpleField) field).astType;

        if (fieldASTType instanceof BasicASTType) {
            String fieldName = ((SimpleField) field).name;
            String typeName = ((BasicASTType) fieldASTType).name;
            final Optional<Type> resolution = scope.resolveType(typeName);
            if (!resolution.isPresent()) {
                log.syntaxError(new SonataSyntaxError(fieldASTType, "Couldn't resolve type '" + typeName + "'"));
            } else {
                Type type = resolution.get();
                if (type.isEntity()) {
                    log.syntaxError(new SonataSyntaxError(field, "Value classes can not depend on entities, but value class '" + valueClass.name + "' has a field named '" + fieldName + "' of type '" + type.name() + "' defined on " + type.definition() + " as an entity."));
                }
            }
        }
    }
}
