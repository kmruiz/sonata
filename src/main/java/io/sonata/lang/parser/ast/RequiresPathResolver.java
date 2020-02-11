/*
 * Copyright (c) 2020 Kevin Mas Ruiz
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package io.sonata.lang.parser.ast;

import io.sonata.lang.source.Source;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

public class RequiresPathResolver implements RequiresResolver {
    private final Set<String> loadedModules;
    private final RequiresPaths requiresPaths;
    private final Function<Source, ScriptNode> parser;

    public RequiresPathResolver(RequiresPaths requiresPaths, Function<Source, ScriptNode> parser) {
        this.loadedModules = new HashSet<>();
        this.requiresPaths = requiresPaths;
        this.parser = parser;
    }

    @Override
    public Optional<ScriptNode> replaceModule(String module) throws IOException {
        if (loadedModules.contains(module)) {
            return Optional.empty();
        }

        loadedModules.add(module);
        if (module.startsWith("std.")) {
            return Optional.ofNullable(parser.apply(resolveStandardModule(module)));
        }

        return Optional.ofNullable(parser.apply(resolveExternalModule(module)));
    }

    private Source resolveStandardModule(String module) {
        return Source.fromResourceModule(module);
    }

    private Source resolveExternalModule(String module) throws IOException {
        final String fileRelativePath = module.replace('.', '/') + ".sn";
        final Path sourceFromCurrentProject = Paths.get(fileRelativePath);
        if (sourceFromCurrentProject.toFile().isFile()) {
            return Source.fromPath(sourceFromCurrentProject);
        }

        final Optional<File> file = requiresPaths.directories.stream().map(path -> new File(path, fileRelativePath)).filter(File::isFile).findFirst();
        if (file.isPresent()) {
            return Source.fromPath(file.get().toPath());
        }

        throw new FileNotFoundException("Module " + module + " not found neither in the current project nor in the requires path:\n" + requiresPaths);
    }
}
