package io.sonata.lang.backend.js;

import io.sonata.lang.backend.Backend;
import io.sonata.lang.backend.BackendCodeGenerator;
import io.sonata.lang.parser.ast.ScriptNode;
import io.sonata.lang.parser.ast.exp.*;
import io.sonata.lang.parser.ast.let.LetFunction;
import io.sonata.lang.parser.ast.let.fn.ExpressionParameter;
import io.sonata.lang.parser.ast.let.fn.SimpleParameter;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.stream.Collectors;

public class JSBackend implements Backend {
    private final ByteArrayOutputStream buffer;
    private int inExpr;

    public JSBackend() {
        this.buffer = new ByteArrayOutputStream(180000);
        this.inExpr = 0;
    }

    private void pushInExpr() {
        this.inExpr++;
    }

    private void popInExpr() {
        this.inExpr--;
    }

    private boolean isInExpr() {
        return this.inExpr > 0;
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
        if (!isInExpr()) {
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

        if (!isInExpr()) {
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
        var base = definition.get(0);
        var output = "function ";
        output += base.letName;
        output += "(";
        output += base.parameters.stream().map(e -> (SimpleParameter) e).map(e -> e.name).collect(Collectors.joining(", "));
        output += "){";
        emit(output);
    }

    @Override
    public void emitFunctionDefinitionEnd(List<LetFunction> definition, BackendCodeGenerator generator) {
        emit(";};");
    }

    @Override
    public void emitBaseFunctionSpecificationBegin(LetFunction base, BackendCodeGenerator generator) {
        emit("return ");
    }

    @Override
    public void emitBaseFunctionSpecificationEnd(LetFunction base, BackendCodeGenerator generator) {

    }

    @Override
    public void emitFunctionSpecificationBegin(LetFunction spec, BackendCodeGenerator generator) {
        var conditions = spec.parameters.stream().filter(e -> e instanceof ExpressionParameter).map(e -> (ExpressionParameter) e).map(e -> e.expression);
        var condString = conditions.map(generator::generateFor).map(String::new).collect(Collectors.joining("&&"));

        emit("if(" + condString + "){return ");
    }

    @Override
    public void emitFunctionSpecificationEnd(LetFunction spec, BackendCodeGenerator generator) {
        emit("}");
    }

    @Override
    public byte[] result() {
        return buffer.toByteArray();
    }

    private void emit(String script) {
        buffer.writeBytes(script.getBytes(Charset.defaultCharset()));
    }
}
