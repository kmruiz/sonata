/*
 * Copyright (c) 2020 Kevin Mas Ruiz
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package io.sonata.lang.parser.ast;

import io.reactivex.subjects.Subject;
import io.sonata.lang.log.CompilerLog;
import io.sonata.lang.source.Source;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RxRequiresNodeNotifier implements RequiresNodeNotifier {
    private final CompilerLog log;
    private final Subject<Source> sources;
    private final Set<String> modulesRequested;
    private final Set<String> modulesFound;

    public RxRequiresNodeNotifier(CompilerLog log, Subject<Source> sources) {
        this.log = log;
        this.sources = sources;
        this.modulesRequested = new HashSet<>();
        this.modulesFound = new HashSet<>();
    }

    @Override
    public void moduleRequired(Source parent, String module) throws IOException {
        if (module.startsWith("std.")) {
            log.requestedModule(module, true);
            sources.onNext(resolveStandardModule(module));
        } else {
            log.requestedModule(module, false);
            sources.onNext(resolveExternalModule(module));
        }

        modulesRequested.add(module);
    }

    @Override
    public void mainModules(List<String> modules) {
        modulesRequested.addAll(modules);
    }

    @Override
    public void loadedModule(String module) {
        if (!modulesFound.contains(module)) {
            modulesFound.add(module);
            log.loadedModule(module);
            if (modulesFound.size() == modulesRequested.size()) {
                sources.onComplete();
            }
        }
    }

    private Source resolveStandardModule(String module) {
        return Source.fromResourceModule(module);
    }

    private Source resolveExternalModule(String module) throws IOException {
        return Source.fromPath(Paths.get(module.replace('.', '/') + ".sn"));
    }
}
