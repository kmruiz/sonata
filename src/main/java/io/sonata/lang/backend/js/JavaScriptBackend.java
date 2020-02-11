/*
 * Copyright (c) 2020 Kevin Mas Ruiz
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package io.sonata.lang.backend.js;

import io.sonata.lang.analyzer.typeSystem.ArrayType;
import io.sonata.lang.analyzer.typeSystem.FunctionType;
import io.sonata.lang.analyzer.typeSystem.Scope;
import io.sonata.lang.backend.CompilerBackend;
import io.sonata.lang.parser.ast.Node;
import io.sonata.lang.parser.ast.ScriptNode;
import io.sonata.lang.parser.ast.classes.contracts.Contract;
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
import java.util.List;
import java.util.Map;
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

        public Context outOfExpression() {
            return new Context(false, isInEntityClass, isInValueClass);
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
    public void compile(Scope scope, ScriptNode node) throws IOException {
        emitPreface(scope);
        emit("(async function (){");
        node.nodes.forEach(e -> this.emitNode(e, new Context(false, false, false)));
        emit("})()");
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

        if (node instanceof Continuation) {
            emitContinuation((Continuation) node, context);
        }

        if (node instanceof Contract) {
            emitContract((Contract) node, context);
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

        if (node instanceof TypeCheckExpression) {
            emitTypeCheckExpression((TypeCheckExpression) node, context);
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

        if (node instanceof Record) {
            emitRecord((Record) node, context);
        }

        if (node instanceof ValueClassEquality) {
            emitValueClassEquality((ValueClassEquality) node, context);
        }
    }

    private void emitLetFunction(LetFunction base, Context context) {
        String internalFunctionName = base.letName;
        List<String> parameterNames = base.parameters.stream().map(e -> (SimpleParameter) e).map(e -> e.name).collect(Collectors.toList());

        if (context.isInEntityClass && !base.isClassLevel) {
            internalFunctionName += "$";
            emitEnqueueFunctionFor(base.letName, internalFunctionName);
        }

        if (context.isInValueClass || base.isClassLevel) {
            emit("self.");
            emit(base.letName);
            emit("=");
        }

        if (context.isInEntityClass || base.isAsync) {
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

    private void emitContinuation(Continuation continuation, Context context) {
        if (context.isInExpression) {
            emit("(");
        }

        emit("await ");
        if (continuation.fanOut) {
            emit("Promise.all(");
        }

        emitNode(continuation.body, context.inExpression());

        if (continuation.fanOut) {
            emit(")");
        }

        if (!context.isInExpression) {
            emit(";");
        } else {
            emit(")");
        }
    }

    private void emitContract(Contract node, Context context) {
        emit("let ", node.name, "={};");
        emit("(function(self){");
        node.body.stream()
                .filter(e -> e instanceof LetFunction && ((LetFunction) e).isClassLevel)
                .forEach(e -> this.emitNode(e, context.inEntityClass()));
        emit("})(", node.name, ");");
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
                emitNode(ex, context.outOfExpression());
            }
        }

        if (context.isInExpression) {
            emit("})();");
        }
    }

    private void emitTypeCheckExpression(TypeCheckExpression typecheck, Context context) {
        if (typecheck.type == Scope.TYPE_ANY) {
            emit("true");
        } else if (typecheck.type == Scope.TYPE_NUMBER) {
            emit("!isNaN(",typecheck.atom,")");
        } else if (typecheck.type == Scope.TYPE_BOOLEAN) {
            emit("(",typecheck.atom,"===true||",typecheck.atom,"===false)");
        } else if (typecheck.type == Scope.TYPE_RECORD) {
            emit("Object.prototype.toString.call(",typecheck.atom,") === '[object Object]'");
        } else if (typecheck.type == Scope.TYPE_STRING) {
            emit("(typeof ", typecheck.atom, "=== 'string')");
        } else if (typecheck.type instanceof ArrayType) {
            emit(typecheck.atom, ".slice!==undefined");
        } else if (typecheck.type.isValue()) {
            emit(typecheck.atom, ".class==='", typecheck.type.name(), "'");
        } else if (typecheck.type.isEntity()) {
            emit("(");
            emit(typecheck.atom, ".class==='",typecheck.type.name(),"'");
            emit("||");
            emit(typecheck.atom, ".contracts.indexOf('",typecheck.type.name(),"')!=-1");
            emit(")");
        } else if (typecheck.type instanceof FunctionType) {
            emit(typecheck.atom, ".apply!==undefined");
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
            emit("}})();");
        } else {
            emit("}");
        }
    }

    private void emitFunctionCall(FunctionCall node, Context context) {
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
        emit(String.join(",", fields));
        emit("){let self={};");
        emit("self.class='", node.name, "';");
        fields.forEach(field -> emit("self.", field, "=", field, ";"));
        node.body.forEach(e -> emitNode(e, context.inValueClass()));
        emit("return self;}");
    }

    private void emitEntityClass(EntityClass node, Context context) {
        final List<String> fields = node.definedFields.stream().map(e -> (SimpleField) e).map(e -> e.name).collect(Collectors.toList());
        emit("function ", node.name, "(");
        emit(String.join(",", fields));
        emit("){let self=ECP('", node.name, "',[",node.implementingContracts.stream().map(Node::representation).map(e -> "'" + e + "'").collect(Collectors.joining(",")),"]);");
        emit("let $stop$ = ST(self);");
        emitEnqueueFunctionFor("stop", "$stop$");
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
        if (node.isAsync) {
            emit("async ");
        }

        emit("function(");
        emit(node.parameters.stream().map(p -> p.name).collect(Collectors.joining(",")));
        emit("){return ");
        emitNode(node.body, (node.isAsync ? context.inExpression().inEntityClass() : context.inExpression()));
        emit("}");
    }

    private void emitMethodReference(MethodReference node, Context context) {
        emitNode(node.receiver, context.inExpression());
        emit(".", node.methodName, !context.isInExpression ? ";" : "");
    }

    private void emitArrayAccess(ArrayAccess node, Context context) {
        emitNode(node.receiver, context.inExpression());
        emit("[");
        emitNode(node.index, context.inExpression());
        emit("]", !context.isInExpression ? ";" : "");
    }

    private void emitRecord(Record node, Context context) {
        emit("({");
        final int values = node.values.size();
        int val = 0;
        for (Map.Entry<Atom, Expression> e : node.values.entrySet()) {
            val++;

            emitNode(e.getKey(), context.inExpression());
            emit(":");
            emitNode(e.getValue(), context.inExpression());

            if (val < values) {
                emit(",");
            }
        }

        emit("})");
    }

    private void emitValueClassEquality(ValueClassEquality node, Context context) {
        emit(node.negate ? "!" : "", "(VCE(");
        emitNode(node.left, context.inExpression());
        emit(",");
        emitNode(node.right, context.inExpression());
        emit("))");

        if (!context.isInExpression) {
            emit(";");
        }
    }

    private void emitPreface(Scope scope) {
        emit("\"use strict\";");
        if (scope.isClassLoaded("IOChannel")) {
            emit("const fsPromises=require('fs').promises;");
        }
        emit("function ECP(c,C){let o={};o._p$=false;o._s$=0;o.class=c;o.contracts=C;o._m$=[];o._i$=SI(DQ(o),0);return o}");
        emit("function _P(){let z,y,x=new Promise(function(r, R){y=r;z=R;});return[x,y,z]}");
        emit("function _$(p){return Array.prototype.slice.call(p)}");
        emit("function SI(a,b){return setInterval(a,b)}");
        emit("function CI(a){clearInterval(a)}");
        emit("function ST(s){const F=function(){s._s$=1;if(s._m$.length>0){setTimeout(s.stop, 0)}else{CI(s._i$)}};F.messageName='stop';return F;}");
        emit("function PS(s,f){return function(){const a=_$(arguments);const v=_P();const p=v[0];const r=v[1];");
        emit("if(s._s$==0)s._m$.push(function(){r(f.apply(null,a))});else r(undefined);");
        emit("return p}}");
        emit("function DQ(s){return function(){if(s._m$.length>0){s._m$.shift()()}}}");
        emit("function VCE(a,b){return JSON.stringify(a)==JSON.stringify(b)}");
    }

    private void emitEnqueueFunctionFor(String baseName, String internalFunctionName) {
        emit("self.", baseName, "=PS(self,", internalFunctionName, ", '", baseName, "');");
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
