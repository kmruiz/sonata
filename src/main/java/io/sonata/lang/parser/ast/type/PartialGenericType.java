package io.sonata.lang.parser.ast.type;

import io.sonata.lang.source.SourcePosition;
import io.sonata.lang.tokenizer.token.SeparatorToken;
import io.sonata.lang.tokenizer.token.Token;

import java.util.Collections;
import java.util.List;

import static io.sonata.lang.javaext.Lists.append;

public class PartialGenericType implements Type {
    public final Type base;
    public final List<Type> parameters;
    public final Type current;

    private PartialGenericType(Type base, List<Type> parameters, Type current) {
        this.base = base;
        this.parameters = parameters;
        this.current = current;
    }

    public static PartialGenericType on(Type base) {
        return new PartialGenericType(base, Collections.emptyList(), EmptyType.instance());
    }

    @Override
    public Type consume(Token token) {
        Type next = current.consume(token);
        if (next == null) {
            if (token instanceof SeparatorToken) {
                SeparatorToken sep = (SeparatorToken) token;
                if (sep.separator.equals(",")) {
                    return new PartialGenericType(base, append(parameters, current), EmptyType.instance());
                }

                if (sep.separator.equals("]")) {
                    if (parameters.size() == 0 && current instanceof EmptyType) {
                        return new ArrayType(base);
                    }

                    return new GenericType(base, append(parameters, current));
                }
            }
        }

        return new PartialGenericType(base, parameters, next);
    }

    @Override
    public String representation() {
        return null;
    }

    @Override
    public SourcePosition definition() {
        return base.definition();
    }
}
