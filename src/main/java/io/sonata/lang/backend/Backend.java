package io.sonata.lang.backend;

import io.sonata.lang.parser.ast.Node;
import io.sonata.lang.parser.ast.ScriptNode;
import io.sonata.lang.parser.ast.classes.fields.Field;
import io.sonata.lang.parser.ast.classes.values.ValueClass;
import io.sonata.lang.parser.ast.exp.*;
import io.sonata.lang.parser.ast.let.LetFunction;
import io.sonata.lang.parser.ast.type.Type;

import java.util.List;

public interface Backend {
    void emitScriptBegin(ScriptNode scriptNode, BackendCodeGenerator generator);
    void emitScriptEnd(ScriptNode scriptNode, BackendCodeGenerator generator);

    void emitAtomExpressionBegin(Atom atom, BackendCodeGenerator generator);
    void emitAtomExpressionEnd(Atom atom, BackendCodeGenerator generator);

    void emitTailExtractionBegin(TailExtraction tailExtraction, BackendCodeGenerator generator);
    void emitTailExtractionEnd(TailExtraction tailExtraction, BackendCodeGenerator generator);

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

    void emitLetConstantBegin(String letName, Type returnType, BackendCodeGenerator generator);
    void emitLetConstantEnd(String letName, Type returnType, BackendCodeGenerator generator);

    void emitMethodReferenceBegin(MethodReference methodReference, BackendCodeGenerator generator);
    void emitMethodReferenceEnd(MethodReference methodReference, BackendCodeGenerator generator);

    void emitMethodReferenceName(String name, BackendCodeGenerator generator);

    void emitBlockExpressionBegin(BlockExpression blockExpression, BackendCodeGenerator generator);
    void emitBlockExpressionExpressionBegin(Node expr, boolean isLast, BackendCodeGenerator generator);
    void emitBlockExpressionExpressionEnd(Node expr, boolean isLast, BackendCodeGenerator generator);
    void emitBlockExpressionEnd(BlockExpression blockExpression, BackendCodeGenerator generator);

    void emitFunctionDefinitionBegin(List<LetFunction> definition, BackendCodeGenerator generator);
    void emitFunctionDefinitionEnd(List<LetFunction> definition, BackendCodeGenerator generator);

    void emitBaseFunctionSpecificationBegin(LetFunction base, BackendCodeGenerator generator);
    void emitBaseFunctionSpecificationEnd(LetFunction base, BackendCodeGenerator generator);

    void emitFunctionSpecificationBegin(LetFunction spec, BackendCodeGenerator generator);
    void emitFunctionSpecificationEnd(LetFunction spec, BackendCodeGenerator generator);

    void emitPreFunctionCall(FunctionCall node, BackendCodeGenerator generator);
    void emitPostFunctionCall(FunctionCall node, BackendCodeGenerator generator);

    void emitPreValueClass(ValueClass vc, BackendCodeGenerator generator);
    void emitValueClassFieldBegin(ValueClass vc, Field field, boolean isLast, BackendCodeGenerator generator);
    void emitValueClassFieldEnd(ValueClass vc, Field field, boolean isLast, BackendCodeGenerator generator);
    void emitValueClassBodyBegin(ValueClass vc, BackendCodeGenerator generator);
    void emitValueClassBodyEnd(ValueClass vc, BackendCodeGenerator generator);
    void emitPostValueClass(ValueClass vc, BackendCodeGenerator generator);

    void emitIfBegin(IfElse ifElse, BackendCodeGenerator generator);
    void emitIfConditionBegin(IfElse ifElse, BackendCodeGenerator generator);
    void emitIfConditionEnd(IfElse ifElse, BackendCodeGenerator generator);
    void emitIfBodyBegin(IfElse ifElse, BackendCodeGenerator generator);
    void emitIfBodyEnd(IfElse ifElse, BackendCodeGenerator generator);
    void emitElseBegin(IfElse ifElse, BackendCodeGenerator generator);
    void emitElseEnd(IfElse ifElse, BackendCodeGenerator generator);


    byte[] result();
}
