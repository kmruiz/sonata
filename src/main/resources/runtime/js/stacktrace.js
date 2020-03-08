function PUSHFRAME(frame, context) {
    const frameWithDate = Object.assign({ when: +new Date() }, frame);
    context.stacktrace = context.stacktrace.concat([frameWithDate]);
}

function GETFRAME(depth, entity) {
    return entity.__context.stacktrace.slice(0, entity.__context.stacktrace.length - depth);
}

function STRSTACKTRACE(entity) {
    return entity.__context.stacktrace.slice(0).reverse().reduce(function (a, b) {
        return a + "\n\t" + b.entityClass + "#" + b.functionName + " at " + b.where;
    }, "")
}