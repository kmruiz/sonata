package io.sonata.lang.cli.command;

import io.sonata.lang.backend.BackendVisitor;
import io.sonata.lang.backend.js.JSBackend;
import io.sonata.lang.parser.Parser;
import io.sonata.lang.source.Source;
import io.sonata.lang.tokenizer.Tokenizer;

import javax.script.ScriptEngineManager;

public class REPL {
    public static void execute() {
        BackendVisitor visitor = new BackendVisitor(JSBackend::new);
        Tokenizer tokenizer = new Tokenizer();

        ScriptEngineManager manager = new ScriptEngineManager();
        var engine = manager.getEngineByName("javascript");

        Source.fromStream("stdin", System.in)
                .read()
                .flatMap(tokenizer::process)
                .scan(Parser.initial(), Parser::reduce)
                .distinctUntilChanged()
                .map(visitor::generateSourceCode)
                .map(String::new)
                .map(engine::eval)
                .map(String::valueOf)
                .distinctUntilChanged()
                .subscribe(output -> System.out.println("> " + output), Throwable::printStackTrace);
    }
}
