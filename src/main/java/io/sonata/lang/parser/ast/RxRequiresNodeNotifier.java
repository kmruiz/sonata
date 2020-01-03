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
