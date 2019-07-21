package io.sonata.lang.parser.ast;

import io.reactivex.subjects.Subject;
import io.sonata.lang.source.Source;

import java.io.IOException;
import java.nio.file.Path;

public class RxRequiresNodeNotifier implements RequiresNodeNotifier {
    private final Subject<Source> sources;

    public RxRequiresNodeNotifier(Subject<Source> sources) {
        this.sources = sources;
    }

    @Override
    public void moduleRequired(Source parent, String module) throws IOException {
        Path foundModule = resolveModule(module);
        Source source = Source.fromPath(foundModule);

        sources.onNext(source);
    }

    @Override
    public void done() {
        sources.onComplete();
    }

    private Path resolveModule(String module) {
        return Path.of(module.replace('.', '/') + ".sn");
    }
}
