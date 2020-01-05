/*
 * Copyright (c) 2020 Kevin Mas Ruiz
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package io.sonata.lang.parser.ast;

import io.reactivex.subjects.Subject;
import io.sonata.lang.source.Source;

import java.io.IOException;
import java.nio.file.Paths;

public class RxRequiresNodeNotifier implements RequiresNodeNotifier {
    private final Subject<Source> sources;

    public RxRequiresNodeNotifier(Subject<Source> sources) {
        this.sources = sources;
    }

    @Override
    public void moduleRequired(Source parent, String module) throws IOException {
        if (module.startsWith("std.")) {
            sources.onNext(resolveStandardModule(module));
        } else {
            sources.onNext(resolveExternalModule(module));
        }
    }

    @Override
    public void done() {
        sources.onComplete();
    }

    private Source resolveStandardModule(String module) {
        return Source.fromResourceModule(module);
    }

    private Source resolveExternalModule(String module) throws IOException {
        return Source.fromPath(Paths.get(module.replace('.', '/') + ".sn"));
    }
}
