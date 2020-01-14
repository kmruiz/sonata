/*
 * Copyright (c) 2020 Kevin Mas Ruiz
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
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
    private static final int BUFFER_SIZE = 2048;
    private static final byte[] EOL = System.lineSeparator().getBytes(Charset.defaultCharset());

    public enum Type {
        FILE, LITERAL, RESOURCE
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

    public static Source fromResourceModule(String moduleName) {
        final String resourceName = "/lib/" + moduleName.replaceAll("\\.", "/") + ".sn";
        return new Source(moduleName, Type.RESOURCE, Source.class.getResourceAsStream(resourceName));
    }

    public final Flowable<SourceCharacter> read() {
        final byte[] buffer = new byte[BUFFER_SIZE];

        return Flowable.create(emitter -> {
            SourcePosition position = SourcePosition.initial(this);

            do {
                final int readByte = inputStream.read(buffer);
                if (readByte == -1) {
                    emitEof(emitter, position);
                    break;
                }

                for (int i = 0; i < readByte; i++) {
                    final char toEmit = (char) buffer[i];
                    position = position.next(toEmit);

                    emitter.onNext(new SourceCharacter(position, toEmit));
                }
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
        for (byte c : EOL) {
            emitter.onNext(new SourceCharacter(position, (char) c));
        }
        emitter.onNext(new SourceCharacter(position.next('\0'),'\0'));
        emitter.onNext(new SourceCharacter(position.next('\0'),'\0'));
    }
}
