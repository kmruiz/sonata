package io.sonata.lang.backend;

import io.sonata.lang.parser.ast.ScriptNode;
import io.sonata.lang.parser.ast.classes.fields.Field;
import io.sonata.lang.parser.ast.classes.values.ValueClass;
import io.sonata.lang.parser.ast.exp.*;
import io.sonata.lang.parser.ast.let.LetFunction;

import java.util.List;

public interface Backend {
    void emitScriptBegin(ScriptNode scriptNode, BackendCodeGenerator generator);
    void emitScriptEnd(ScriptNode scriptNode, BackendCodeGenerator generator);

    void emitAtomExpressionBegin(Atom atom, BackendCodeGenerator generator);
    void emitAtomExpressionEnd(Atom atom, BackendCodeGenerator generator);

    void emitSimpleExpressionBegin(SimpleExpression expression, BackendCodeGenerator generator);
    void emitSimpleExpressionOperator(String operator, BackendCodeGenerator generator);
    void emitSimpleExpressionEnd(SimpleExpression expression, BackendCodeGenerator generator);

    void emitArrayBegin(LiteralArray array, BackendCodeGenerator generator);
    void emitArraySeparator(LiteralArray array, boolean isLast, BackendCodeGenerator generator);
    void emitArrayEnd(LiteralArray array, BackendCodeGenerator generator);

    void emitArrayAccessBegin(ArrayAccess access, BackendCodeGenerator generator);
    void emitArrayAccessIndex(String index, BackendCodeGenerator generator);
    void emitArrayAccessEnd(ArrayAccess access, BackendCodeGenerator generator);

    void emitPriorityExpressionBegin(PriorityExpression expression, BackendCodeGenerator generator);
    void emitPriorityExpressionEnd(PriorityExpression expression, BackendCodeGenerator generator);

    void emitFunctionCallBegin(FunctionCall functionCall, BackendCodeGenerator generator);
    void emitFunctionCallArgumentBegin(Expression expression, boolean isLast, BackendCodeGenerator generator);
    void emitFunctionCallArgumentEnd(Expression expression, boolean isLast, BackendCodeGenerator generator);
    void emitFunctionCallEnd(FunctionCall functionCall, BackendCodeGenerator generator);

    void emitMethodReferenceBegin(MethodReference methodReference, BackendCodeGenerator generator);
    void emitMethodReferenceEnd(MethodReference methodReference, BackendCodeGenerator generator);

    void emitMethodReferenceName(String name, BackendCodeGenerator generator);

    void emitFunctionDefinitionBegin(List<LetFunction> definition, BackendCodeGenerator generator);
    void emitFunctionDefinitionEnd(List<LetFunction> definition, BackendCodeGenerator generator);

    void emitBaseFunctionSpecificationBegin(LetFunction base, BackendCodeGenerator generator);
    void emitBaseFunctionSpecificationEnd(LetFunction base, BackendCodeGenerator generator);

    void emitFunctionSpecificationBegin(LetFunction spec, BackendCodeGenerator generator);
    void emitFunctionSpecificationEnd(LetFunction spec, BackendCodeGenerator generator);

    void emitPreFunctionCall(FunctionCall node, BackendVisitor backendVisitor);
    void emitPostFunctionCall(FunctionCall node, BackendVisitor backendVisitor);

    void emitPreValueClass(ValueClass vc, BackendVisitor backendVisitor);
    void emitValueClassFieldBegin(ValueClass vc, Field field, boolean isLast, BackendVisitor backendVisitor);
    void emitValueClassFieldEnd(ValueClass vc, Field field, boolean isLast, BackendVisitor backendVisitor);
    void emitValueClassBodyBegin(ValueClass vc, BackendVisitor backendVisitor);
    void emitValueClassBodyEnd(ValueClass vc, BackendVisitor backendVisitor);
    void emitPostValueClass(ValueClass vc, BackendVisitor backendVisitor);

    byte[] result();
}
