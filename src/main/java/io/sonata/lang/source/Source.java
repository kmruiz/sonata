package io.sonata.lang.source;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

public class Source implements AutoCloseable {
    public enum Type {
        FILE, LITERAL
    }

    public final String name;
    public final Type type;
    private final InputStream inputStream;

    private Source(String name, Type type, InputStream inputStream) {
        this.name = name;
        this.type = type;
        this.inputStream = inputStream;
    }

    public static Source fromPath(Path path) throws IOException {
        return new Source(path.toString(), Type.FILE, new BufferedInputStream(Files.newInputStream(path)));
    }

    public static Source fromLiteral(String literal) {
        return new Source(literal, Type.LITERAL, new ByteArrayInputStream(literal.getBytes(Charset.defaultCharset())));
    }

    public static Source endOfProgram() {
        return fromLiteral("\0");
    }

    public final Flowable<SourceCharacter> read() {
        return Flowable.create(emitter -> {
            SourcePosition position = SourcePosition.initial(this);

            do {
                final int readByte = inputStream.read();
                if (readByte == -1) {
                    emitEof(emitter, position);
                    break;
                }

                final char toEmit = (char) readByte;
                position = position.next(toEmit);

                emitter.onNext(new SourceCharacter(position, toEmit));
            } while (true);

            emitter.onComplete();
            close();
        }, BackpressureStrategy.BUFFER);
    }

    @Override
    public void close() throws Exception {
        inputStream.close();
    }

    private void emitEof(FlowableEmitter<SourceCharacter> emitter, SourcePosition position) {
        byte[] lineSeparator = System.lineSeparator().getBytes(Charset.defaultCharset());
        for (int i = 0; i < 10; i++) {
            for (byte c : lineSeparator) {
                emitter.onNext(new SourceCharacter(position, (char) c));
            }
        }
    }
}
