package io.sonata.lang.backend.js;

import io.sonata.lang.backend.CompilerBackend;
import io.sonata.lang.parser.ast.Node;
import io.sonata.lang.parser.ast.ScriptNode;
import io.sonata.lang.parser.ast.classes.entities.EntityClass;
import io.sonata.lang.parser.ast.classes.fields.SimpleField;
import io.sonata.lang.parser.ast.classes.values.ValueClass;
import io.sonata.lang.parser.ast.exp.*;
import io.sonata.lang.parser.ast.let.LetConstant;
import io.sonata.lang.parser.ast.let.LetFunction;
import io.sonata.lang.parser.ast.let.fn.SimpleParameter;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class JavaScriptBackend implements CompilerBackend {
    private static class Context {
        public final boolean isInExpression;
        public final boolean isInEntityClass;
        public final boolean isInValueClass;

        private Context(boolean isInExpression, boolean isInEntityClass, boolean isInValueClass) {
            this.isInExpression = isInExpression;
            this.isInEntityClass = isInEntityClass;
            this.isInValueClass = isInValueClass;
        }

        public Context inExpression() {
            return new Context(true, isInEntityClass, isInValueClass);
        }

        public Context inEntityClass() {
            return new Context(isInExpression, true, isInValueClass);
        }

        public Context inValueClass() {
            return new Context(isInExpression, isInEntityClass, true);
        }
    }

    private final Writer outputWriter;

    public JavaScriptBackend(Writer outputWriter) {
        this.outputWriter = outputWriter;
    }

    @Override
    public void compile(ScriptNode node) throws IOException {
        emitPreface();
        node.nodes.forEach(e -> this.emitNode(e, new Context(false, false, false)));
        outputWriter.flush();
        outputWriter.close();
    }

    private void emitNode(Node node, Context context) {
        if (node instanceof LetFunction) {
            emitLetFunction((LetFunction) node, context);
        }

        if (node instanceof Atom) {
            emitAtom((Atom) node, context);
        }

        if (node instanceof TailExtraction) {
            emitTrailExtraction((TailExtraction) node, context);
        }

        if (node instanceof SimpleExpression) {
            emitSimpleExpression((SimpleExpression) node, context);
        }

        if (node instanceof PriorityExpression) {
            emitPriorityExpression((PriorityExpression) node, context);
        }

        if (node instanceof BlockExpression) {
            emitBlockExpression((BlockExpression) node, context);
        }

        if (node instanceof LetConstant) {
            emitLetConstant((LetConstant) node, context);
        }

        if (node instanceof IfElse) {
            emitIfElse((IfElse) node, context);
        }

        if (node instanceof FunctionCall) {
            emitFunctionCall((FunctionCall) node, context);
        }

        if (node instanceof ValueClass) {
            emitValueClass((ValueClass) node, context);
        }

        if (node instanceof EntityClass) {
            emitEntityClass((EntityClass) node, context);
        }

        if (node instanceof LiteralArray) {
            emitLiteralArray((LiteralArray) node, context);
        }

        if (node instanceof Lambda) {
            emitLambda((Lambda) node, context);
        }

        if (node instanceof MethodReference) {
            emitMethodReference((MethodReference) node, context);
        }

        if (node instanceof ArrayAccess) {
            emitArrayAccess((ArrayAccess) node, context);
        }
    }

    private void emitLetFunction(LetFunction base, Context context) {
        String internalFunctionName = base.letName;
        List<String> parameterNames = base.parameters.stream().map(e -> (SimpleParameter) e).map(e -> e.name).collect(Collectors.toList());

        if (context.isInEntityClass) {
            internalFunctionName += "$";
            emitEnqueueFunctionFor(base.letName, internalFunctionName, parameterNames);
        }

        if (context.isInValueClass) {
            emit("self.");
            emit(base.letName);
            emit("=");
        }

        if (context.isInEntityClass) {
            emit("async ");
        }

        emit("function ", internalFunctionName, "(", String.join(", ", parameterNames), "){");

        if (base.body instanceof BlockExpression || base.body instanceof IfElse) {
            emitNode(base.body, context);
        } else {
            emit("return ");
            emitNode(base.body, context.inExpression());
        }
        emit("};");
    }

    private void emitAtom(Atom atom, Context context) {
        emit(atom.value, !context.isInExpression ? ";" : "");
    }

    private void emitTrailExtraction(TailExtraction node, Context context) {
        emitNode(node.expression, context.inExpression());
        emit(".slice(", String.valueOf(node.fromIndex), ")", !context.isInExpression ? ";" : "");
    }

    private void emitSimpleExpression(SimpleExpression node, Context context) {
        emitNode(node.leftSide, context.inExpression());
        emit(node.operator);
        emitNode(node.rightSide, context.inExpression());
        emit(!context.isInExpression ? ";" : "");
    }

    private void emitPriorityExpression(PriorityExpression node, Context context) {
        emit("(");
        emitNode(node.expression, context.inExpression());
        emit(")", !context.isInExpression ? ";" : "");
    }

    private void emitBlockExpression(BlockExpression node, Context context) {
        if (context.isInExpression) {
            if (context.isInEntityClass) {
                emit("(async function(){");
            } else {
                emit("(function(){");
            }
        }

        final int exprCount = node.expressions.size();
        int count = 0;

        for (Expression ex : node.expressions) {
            count++;
            boolean isLast = count == exprCount;

            if (isLast && !(ex instanceof IfElse)) {
                emit("return ");
                emitNode(ex, context.inExpression());
            } else {
                emitNode(ex, context);
            }
        }

        if (context.isInExpression) {
            emit("})();");
        }
    }

    private void emitLetConstant(LetConstant node, Context context) {
        emit("let ", node.letName, "=");
        emitNode(node.body, context.inExpression());
        emit(";");
    }

    private void emitIfElse(IfElse node, Context context) {
        if (context.isInExpression) {
            emit("(function(){if(");
        } else {
            emit("if(");
        }

        emitNode(node.condition, context.inExpression());
        emit("){");
        if (!(node.whenTrue instanceof BlockExpression)) {
            emit("return ");
        }
        emitNode(node.whenTrue, context);

        if (node.whenFalse != null) {
            emit("}else{");
            if (!(node.whenFalse instanceof BlockExpression)) {
                emit("return ");
            }

            emitNode(node.whenFalse, context);
        }

        if (context.isInExpression) {
            emit("})();");
        } else {
            emit("}");
        }
    }

    private void emitFunctionCall(FunctionCall node, Context context) {
        if (context.isInExpression && context.isInEntityClass) {
            emit("await ");
        }

        emitNode(node.receiver, context.inExpression());
        emit("(");
        final int args = node.arguments.size();
        int arg = 0;
        for (Expression expr : node.arguments) {
            arg++;
            emitNode(expr, context.inExpression());
            if (arg < args) {
                emit(",");
            }
        }
        emit(")", !context.isInExpression ? ";" : "");
    }

    private void emitValueClass(ValueClass node, Context context) {
        final List<String> fields = node.definedFields.stream().map(e -> (SimpleField) e).map(e -> e.name).collect(Collectors.toList());
        emit("function ", node.name, "(");
        emit(fields.stream().collect(Collectors.joining(",")));
        emit("){let self={};");
        emit("self.class='", node.name, "';");
        fields.forEach(field -> emit("self.", field, "=", field, ";"));
        node.body.forEach(e -> emitNode(e, context.inValueClass()));
        emit("return self;}");
    }

    private void emitEntityClass(EntityClass node, Context context) {
        final List<String> fields = node.definedFields.stream().map(e -> (SimpleField) e).map(e -> e.name).collect(Collectors.toList());
        emit("function ", node.name, "(");
        emit(fields.stream().collect(Collectors.joining(",")));
        emit("){let self={};");
        emit("self.class='", node.name, "';");
        emit("self._m$=[];");
        emit("self._i$=SI(DQ(self),0);");
        emitEnqueueFunctionFor("stop","$stop$", Collections.emptyList());
        emit("function $stop$(){CI(self._i$)};");
        fields.forEach(field -> emit("self.", field, "=", field, ";"));
        node.body.forEach(e -> emitNode(e, context.inEntityClass()));
        emit("return self;}");
    }

    private void emitLiteralArray(LiteralArray node, Context context) {
        emit("[");
        node.expressions.forEach(n -> {
            emitNode(n, context.inExpression());
            emit(",");
        });
        emit("]", !context.isInExpression ? ";" : "");
    }

    private void emitLambda(Lambda node, Context context) {
        emit("function(");
        emit(node.parameters.stream().map(p -> p.name).collect(Collectors.joining(",")));
        emit("){return ");
        emitNode(node.body, context.inExpression());
        emit("}");
    }

    private void emitMethodReference(MethodReference node, Context context) {
        emitNode(node.receiver, context);
        emit(".", node.methodName, !context.isInExpression ? ";" : "");
    }

    private void emitArrayAccess(ArrayAccess node, Context context) {
        emitNode(node.receiver, context);
        emit("[", node.index, "]", !context.isInExpression ? ";" : "");
    }

    private void emitPreface() {
        emit("\"use strict\";");
        emit("function _(){let y,x=new Promise(function(r){y=r});return[x,y]}");
        emit("function _$(p){return Array.prototype.slice.call(p)}");
        emit("function SI(a,b){return setInterval(a,b)}");
        emit("function CI(a){clearInterval(a)}");
        emit("function PS(s,f){return function(){const a=_$(arguments);const v=_();const p=v[0];const r=v[1];");
        emit("s._m$.push(function(){r(f.apply(null,a))});");
        emit("return p}}");
        emit("function DQ(s){return function(){if(s._m$.length>0){s._m$.shift()()}}}");
    }

    private void emitEnqueueFunctionFor(String baseName, String internalFunctionName, List<String> parameterNames) {
        emit("self.", baseName, "=PS(self,", internalFunctionName, ");const ", baseName, "=self.",baseName,";");
    }

    private void emit(String... args) {
        Arrays.stream(args).forEach(this::safeWrite);
    }

    private void safeWrite(String args) {
        try {
            outputWriter.append(args);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
