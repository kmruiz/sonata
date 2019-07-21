package io.sonata.lang.parser.ast;

import io.sonata.lang.source.Source;

import java.io.IOException;

public interface RequiresNodeNotifier {
    void moduleRequired(Source parent,  String module) throws IOException;
    void done();
}
