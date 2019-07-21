package io.sonata.lang.cli.command;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import io.sonata.lang.backend.BackendVisitor;
import io.sonata.lang.backend.js.JSBackend;
import io.sonata.lang.parser.Parser;
import io.sonata.lang.parser.ast.RequiresNodeNotifier;
import io.sonata.lang.parser.ast.RxRequiresNodeNotifier;
import io.sonata.lang.source.Source;
import io.sonata.lang.tokenizer.Tokenizer;

import javax.script.ScriptEngineManager;

public class REPL {
    public static void execute() {
        BackendVisitor visitor = new BackendVisitor(JSBackend::new);
        Tokenizer tokenizer = new Tokenizer();

        ScriptEngineManager manager = new ScriptEngineManager();
        var engine = manager.getEngineByName("javascript");

        Subject<Source> requires = PublishSubject.create();
        RequiresNodeNotifier notifier = new RxRequiresNodeNotifier(requires);

        Flowable.fromArray(Source.fromStream("stdin", System.in))
                .concatWith(requires.toFlowable(BackpressureStrategy.BUFFER))
                .flatMap(Source::read)
                .flatMap(tokenizer::process)
                .scan(Parser.initial(notifier), Parser::reduce)
                .distinctUntilChanged()
                .map(visitor::generateSourceCode)
                .map(String::new)
                .map(engine::eval)
                .map(String::valueOf)
                .distinctUntilChanged()
                .subscribe(output -> System.out.println("> " + output), Throwable::printStackTrace);
    }
}
