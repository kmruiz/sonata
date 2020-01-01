package io.sonata.lang.analyzer.typeSystem.exception;

import io.sonata.lang.source.SourcePosition;

public class TypeCanNotBeReassignedException extends InvalidTypeUsageException {
    private final SourcePosition initialAssignment;

    public TypeCanNotBeReassignedException(SourcePosition initialAssignment) {
        this.initialAssignment = initialAssignment;
    }

    public SourcePosition initialAssignment() {
        return initialAssignment;
    }
}
