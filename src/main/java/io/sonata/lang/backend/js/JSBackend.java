package io.sonata.lang.backend.js;

import io.sonata.lang.backend.Backend;
import io.sonata.lang.backend.BackendCodeGenerator;
import io.sonata.lang.backend.BackendVisitor;
import io.sonata.lang.parser.ast.ScriptNode;
import io.sonata.lang.parser.ast.classes.fields.Field;
import io.sonata.lang.parser.ast.classes.values.ValueClass;
import io.sonata.lang.parser.ast.exp.*;
import io.sonata.lang.parser.ast.let.LetFunction;
import io.sonata.lang.parser.ast.let.fn.ExpressionParameter;
import io.sonata.lang.parser.ast.let.fn.SimpleParameter;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        var base = definition.get(0);

        if (inClass) {
            emit("body.");
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
        var conditions = spec.parameters.stream().filter(e -> e instanceof ExpressionParameter).map(e -> (ExpressionParameter) e).map(e -> e.expression).collect(Collectors.toList());
        var condArray = new ArrayList<String>();

        var extractions = conditions.stream().filter(e -> e instanceof LiteralArray).map(e -> (LiteralArray) e).collect(Collectors.toList());
        if (!extractions.isEmpty()) {
            var paramIdx = new AtomicInteger(0);

            extractions.forEach(param -> {
                var hasTailExtr = new AtomicBoolean(false);
                var arrayName = parameterNames.get(paramIdx.getAndIncrement());

                var arrayIndex = new AtomicInteger(0);
                param.expressions.forEach(expr -> {
                    if (expr instanceof Atom) {
                        var atom = (Atom) expr;
                        var name = atom.value;
                        var index = arrayIndex.getAndIncrement();

                        if (atom.type == Atom.Type.IDENTIFIER) {
                            emit("var ");
                            emit(name);
                            emit("=");
                            emit(arrayName);
                            emit("[");
                            emit(String.valueOf(index));
                            emit("];");
                        } else {
                            condArray.add(String.format("%s[%d] === %s", arrayName, index, atom.representation()));
                        }

                    } else if (expr instanceof TailExtraction) {
                        var tailExtr = ((TailExtraction) expr);

                        emit("var ");
                        emit(tailExtr.expression.representation());
                        emit("=");
                        emit(arrayName);
                        emit(".slice(");
                        emit(String.valueOf(arrayIndex.getAndIncrement()));
                        emit(");");

                        hasTailExtr.set(true);
                    }
                });

                if (hasTailExtr.get()) {
                    condArray.add(String.format("%s.length >= %s", arrayName, param.expressions.size()));
                } else {
                    condArray.add(String.format("%s.length === %s", arrayName, param.expressions.size()));
                }
            });
        }

        var notExtractions = conditions.stream().filter(e -> !(e instanceof LiteralArray));
        var condString = Stream.concat(
                condArray.stream(),
                notExtractions.map(generator::generateFor).map(String::new)
        ).collect(Collectors.joining("&&"));

        emit("if(" + condString + "){return ");
    }

    @Override
    public void emitFunctionSpecificationEnd(LetFunction spec, BackendCodeGenerator generator) {
        emit("}");
    }

    @Override
    public void emitPreFunctionCall(FunctionCall node, BackendVisitor backendVisitor) {
        pushInExpr();
    }

    @Override
    public void emitPreValueClass(ValueClass vc, BackendVisitor backendVisitor) {
        emit("function ");
        emit(vc.name);
        emit("(");
    }

    @Override
    public void emitValueClassFieldBegin(ValueClass vc, Field field, boolean isLast, BackendVisitor backendVisitor) {
        emit(field.name());
    }

    @Override
    public void emitValueClassFieldEnd(ValueClass vc, Field field, boolean isLast, BackendVisitor backendVisitor) {
        if (isLast) {
            emit("){");
        } else {
            emit(",");
        }
    }

    @Override
    public void emitValueClassBodyBegin(ValueClass vc, BackendVisitor backendVisitor) {
        inClass = true;
    }

    @Override
    public void emitValueClassBodyEnd(ValueClass vc, BackendVisitor backendVisitor) {
        inClass = false;
        emit("return body;};");
    }

    @Override
    public void emitPostValueClass(ValueClass vc, BackendVisitor backendVisitor) {
        emit("var body={};");
        emit("body.class='");
        emit(vc.name);
        emit("';");
        vc.definedFields.forEach(field -> {
            emit("body.");
            emit(field.name());
            emit("=");
            emit(field.name());
            emit(";");
        });
    }

    @Override
    public void emitPostFunctionCall(FunctionCall node, BackendVisitor backendVisitor) {
        popInExpr();

        if (isNotInExpression()) {
            emit(";");
        }
    }

    @Override
    public byte[] result() {
        return buffer.toByteArray();
    }

    private void emit(String script) {
        buffer.writeBytes(script.getBytes(Charset.defaultCharset()));
    }
}
