let _globalST = [];

function PUSHFRAME(frame, context) {
    const frameWithDate = Object.assign({ when: +new Date() }, frame);
    context.stacktrace = context.stacktrace.concat([frameWithDate]);
    _globalST = context.stacktrace;
}

function GETFRAME(depth, entity) {
    return entity.__context.stacktrace.slice(0).reverse()[4];
}

function STRSTACKTRACE(entity) {
    return entity.__context.stacktrace.slice(0).reverse().reduce(function (a, b) {
        return a + "\n\t" + b.entityClass + "#" + b.functionName + " at " + b.where;
    }, "")
}