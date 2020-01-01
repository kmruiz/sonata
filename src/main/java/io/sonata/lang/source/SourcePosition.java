package io.sonata.lang.source;

public class SourcePosition {
    public final Source source;
    public final int line;
    public final int column;

    private SourcePosition(Source source, int line, int column) {
        this.source = source;
        this.line = line;
        this.column = column;
    }

    public static SourcePosition initial(Source source) {
        return new SourcePosition(source, 1, 0);
    }

    public final SourcePosition next(char byChar) {
        if (byChar == '\n') {
            return new SourcePosition(source, line + 1, 0);
        }
        return new SourcePosition(source, line, column + 1);
    }

    @Override
    public String toString() {
        return String.format("[%s:%d:%d]", source.name, line, column);
    }
}
