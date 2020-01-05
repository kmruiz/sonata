package io.sonata.lang.parser.ast.exp;

import io.sonata.lang.source.SourcePosition;

import java.util.Map;
import java.util.stream.Collectors;

public class Record extends ComposedExpression implements Expression {
    public final SourcePosition definition;
    public final Map<Atom, Expression> values;

    public Record(SourcePosition definition, Map<Atom, Expression> values) {
        this.definition = definition;
        this.values = values;
    }

    @Override
    public SourcePosition definition() {
        return definition;
    }

    @Override
    public String representation() {
        return values.entrySet().stream().map(kv -> kv.getKey() + ":" + kv.getValue()).collect(Collectors.joining(",", "{", "}"));
    }
}
