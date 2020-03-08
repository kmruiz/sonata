function PUSHFRAME(frame, context) {
    context.stacktrace = (context.stacktrace || []).concat([frame]);
}