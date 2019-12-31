package io.sonata.lang.backend.js;

import io.reactivex.Flowable;
import io.sonata.lang.backend.Backend;
import io.sonata.lang.backend.BackendCodeGenerator;
import io.sonata.lang.parser.ast.Node;
import io.sonata.lang.parser.ast.ScriptNode;
import io.sonata.lang.parser.ast.classes.fields.Field;
import io.sonata.lang.parser.ast.classes.values.ValueClass;
import io.sonata.lang.parser.ast.exp.*;
import io.sonata.lang.parser.ast.let.LetFunction;
import io.sonata.lang.parser.ast.let.fn.ExpressionParameter;
import io.sonata.lang.parser.ast.let.fn.SimpleParameter;
import io.sonata.lang.parser.ast.type.Type;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.sonata.lang.javaext.Objects.requireNonNullElse;

public class JSBackend implements Backend {
    private final ByteArrayOutputStream buffer;
    private int inExpr;
    private List<String> parameterNames;
    private boolean inClass;

    public JSBackend() {
        this.buffer = new ByteArrayOutputStream(180000);
        this.inExpr = 0;
        this.parameterNames = null;
        this.inClass = false;
    }

    private void pushInExpr() {
        this.inExpr++;
    }

    private void popInExpr() {
        this.inExpr--;
    }

    private boolean isNotInExpression() {
        return this.inExpr <= 0;
    }

    @Override
    public void emitScriptBegin(ScriptNode scriptNode, BackendCodeGenerator generator) {
        emit("\"use strict\";");
    }

    @Override
    public void emitScriptEnd(ScriptNode scriptNode, BackendCodeGenerator generator) {

    }

    @Override
    public void emitAtomExpressionBegin(Atom atom, BackendCodeGenerator generator) {
        emit(atom.value);
    }

    @Override
    public void emitAtomExpressionEnd(Atom atom, BackendCodeGenerator generator) {
        if (isNotInExpression()) {
            emit(";");
        }
    }

    @Override
    public void emitTailExtractionBegin(TailExtraction tailExtraction, BackendCodeGenerator generator) {
        pushInExpr();
    }

    @Override
    public void emitTailExtractionEnd(TailExtraction tailExtraction, BackendCodeGenerator generator) {
        popInExpr();
        emit(".slice(");
        emit(String.valueOf(requireNonNullElse(tailExtraction.fromIndex, 0)));
        emit(")");

        if (isNotInExpression()) {
            emit(";");
        }
    }

    @Override
    public void emitSimpleExpressionBegin(SimpleExpression expression, BackendCodeGenerator generator) {
        pushInExpr();
    }

    @Override
    public void emitSimpleExpressionOperator(String operator, BackendCodeGenerator generator) {
        emit(operator);
    }

    @Override
    public void emitSimpleExpressionEnd(SimpleExpression expression, BackendCodeGenerator generator) {
        popInExpr();
    }

    @Override
    public void emitArrayBegin(LiteralArray array, BackendCodeGenerator generator) {
        pushInExpr();
        emit("[");
    }

    @Override
    public void emitArraySeparator(LiteralArray array, boolean isLast, BackendCodeGenerator generator) {
        if (!isLast) {
            emit(",");
        }
    }

    @Override
    public void emitArrayEnd(LiteralArray array, BackendCodeGenerator generator) {
        emit("]");
        popInExpr();
    }

    @Override
    public void emitArrayAccessBegin(ArrayAccess access, BackendCodeGenerator generator) {
        pushInExpr();
    }

    @Override
    public void emitArrayAccessIndex(String index, BackendCodeGenerator generator) {
        emit("[");
        emit(index);
        emit("]");
    }

    @Override
    public void emitArrayAccessEnd(ArrayAccess access, BackendCodeGenerator generator) {
        popInExpr();
    }

    @Override
    public void emitPriorityExpressionBegin(PriorityExpression expression, BackendCodeGenerator generator) {
        emit("(");
    }

    @Override
    public void emitPriorityExpressionEnd(PriorityExpression expression, BackendCodeGenerator generator) {
        emit(")");
    }

    @Override
    public void emitFunctionCallBegin(FunctionCall functionCall, BackendCodeGenerator generator) {
        emit("(");
        pushInExpr();
    }

    @Override
    public void emitFunctionCallArgumentBegin(Expression expression, boolean isLast, BackendCodeGenerator generator) {
    }

    @Override
    public void emitFunctionCallArgumentEnd(Expression expression, boolean isLast, BackendCodeGenerator generator) {
        emit(isLast ? "" : ",");
    }

    @Override
    public void emitFunctionCallEnd(FunctionCall functionCall, BackendCodeGenerator generator) {
        emit(")");
        popInExpr();

        if (isNotInExpression()) {
            emit(";");
        }
    }

    @Override
    public void emitMethodReferenceBegin(MethodReference methodReference, BackendCodeGenerator generator) {
        pushInExpr();
    }

    @Override
    public void emitMethodReferenceEnd(MethodReference methodReference, BackendCodeGenerator generator) {
        popInExpr();
    }

    @Override
    public void emitMethodReferenceName(String name, BackendCodeGenerator generator) {
        emit(".");
        emit(name);
    }

    @Override
    public void emitFunctionDefinitionBegin(List<LetFunction> definition, BackendCodeGenerator generator) {
        LetFunction base = definition.get(0);

        if (inClass) {
            emit("self.");
            emit(base.letName);
            emit("=");
        }

        emit("function ");

        this.parameterNames = base.parameters.stream().map(e -> (SimpleParameter) e).map(e -> e.name).collect(Collectors.toList());

        emit(base.letName);
        emit("(");
        emit(String.join(", ", parameterNames));
        emit("){");
    }

    @Override
    public void emitFunctionDefinitionEnd(List<LetFunction> definition, BackendCodeGenerator generator) {
        emit("};");
        this.parameterNames = null;
    }

    @Override
    public void emitBaseFunctionSpecificationBegin(LetFunction base, BackendCodeGenerator generator) {
        if (base.body != null) {
            emit("return ");
        }
    }

    @Override
    public void emitBaseFunctionSpecificationEnd(LetFunction base, BackendCodeGenerator generator) {

    }

    @Override
    public void emitFunctionSpecificationBegin(LetFunction spec, BackendCodeGenerator generator) {
        List<Expression> conditions = spec.parameters.stream().filter(e -> e instanceof ExpressionParameter).map(e -> (ExpressionParameter) e).map(e -> e.expression).collect(Collectors.toList());
        Stream<Expression> notExtractions = conditions.stream().filter(e -> !(e instanceof LiteralArray));
        String condString = notExtractions.map(generator::generateFor).map(Flowable::blockingSingle).map(String::new).collect(Collectors.joining("&&"));

        emit("if(" + condString + "){return ");
    }

    @Override
    public void emitFunctionSpecificationEnd(LetFunction spec, BackendCodeGenerator generator) {
        emit("}");
    }

    @Override
    public void emitPreFunctionCall(FunctionCall node, BackendCodeGenerator generator) {
        pushInExpr();
    }

    @Override
    public void emitPreValueClass(ValueClass vc, BackendCodeGenerator generator) {
        emit("function ");
        emit(vc.name);
        emit("(");
    }

    @Override
    public void emitValueClassFieldBegin(ValueClass vc, Field field, boolean isLast, BackendCodeGenerator generator) {
        emit(field.name());
    }

    @Override
    public void emitValueClassFieldEnd(ValueClass vc, Field field, boolean isLast, BackendCodeGenerator generator) {
        if (isLast) {
            emit("){");
        } else {
            emit(",");
        }
    }

    @Override
    public void emitValueClassFieldless(ValueClass vc, BackendCodeGenerator generator) {
        emit("){");
    }

    @Override
    public void emitValueClassBodyBegin(ValueClass vc, BackendCodeGenerator generator) {
        inClass = true;
    }

    @Override
    public void emitValueClassBodyEnd(ValueClass vc, BackendCodeGenerator generator) {
        inClass = false;
        emit("return self;};");
    }

    @Override
    public void emitPostValueClass(ValueClass vc, BackendCodeGenerator generator) {
        emit("let self={};");
        emit("self.class='");
        emit(vc.name);
        emit("';");
        vc.definedFields.forEach(field -> {
            emit("self.");
            emit(field.name());
            emit("=");
            emit(field.name());
            emit(";");
        });
    }

    @Override
    public void emitPostFunctionCall(FunctionCall node, BackendCodeGenerator generator) {
        popInExpr();

        if (isNotInExpression()) {
            emit(";");
        }
    }

    @Override
    public void emitLetConstantBegin(String letName, Type returnType, BackendCodeGenerator generator) {
        emit("let ");
        emit(letName);
        emit("=");
        pushInExpr();
    }

    @Override
    public void emitLetConstantEnd(String letName, Type returnType, BackendCodeGenerator generator) {
        emit(";");
        popInExpr();
    }

    @Override
    public void emitBlockExpressionBegin(BlockExpression blockExpression, BackendCodeGenerator generator) {
        emit("(function () {");
    }

    @Override
    public void emitBlockExpressionExpressionBegin(Node expr, boolean isLast, BackendCodeGenerator generator) {
        if (isLast) {
            emit("return ");
        }
    }

    @Override
    public void emitBlockExpressionExpressionEnd(Node expr, boolean isLast, BackendCodeGenerator generator) {
    }

    @Override
    public void emitBlockExpressionEnd(BlockExpression blockExpression, BackendCodeGenerator generator) {
        emit("})();");
    }

    @Override
    public void emitIfBegin(IfElse ifElse, BackendCodeGenerator generator) {
        emit("if");
    }

    @Override
    public void emitIfConditionBegin(IfElse ifElse, BackendCodeGenerator generator) {
        emit("(");
        pushInExpr();
    }

    @Override
    public void emitIfConditionEnd(IfElse ifElse, BackendCodeGenerator generator) {
        emit(")");
        popInExpr();
    }

    @Override
    public void emitIfBodyBegin(IfElse ifElse, BackendCodeGenerator generator) {
        emit("{return ");
    }

    @Override
    public void emitIfBodyEnd(IfElse ifElse, BackendCodeGenerator generator) {
        emit("}");
    }

    @Override
    public void emitElseBegin(IfElse ifElse, BackendCodeGenerator generator) {
        emit("else{");
    }

    @Override
    public void emitElseEnd(IfElse ifElse, BackendCodeGenerator generator) {
        emit("}");
    }

    @Override
    public void emitLambdaDefinitionBegin(Lambda lambda, BackendCodeGenerator generator) {
        String params = lambda.parameters.stream().map(e -> e.name).collect(Collectors.joining(","));
        emit("function (");
        emit(params);
        emit(")");
    }

    @Override
    public void emitLambdaDefinitionEnd(Lambda lambda, BackendCodeGenerator generator) {

    }

    @Override
    public void emitLambdaBodyBegin(Lambda lambda, BackendCodeGenerator generator) {
        emit("{ return ");
    }

    @Override
    public void emitLambdaBodyEnd(Lambda lambda, BackendCodeGenerator generator) {
        emit("}");
    }

    @Override
    public byte[] result() {
        return buffer.toByteArray();
    }

    private void emit(String script) {
        try {
            buffer.write(script.getBytes(Charset.defaultCharset()));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
