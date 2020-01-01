package io.sonata.lang.parser.ast.type;

import io.sonata.lang.source.SourcePosition;
import io.sonata.lang.tokenizer.token.SeparatorToken;
import io.sonata.lang.tokenizer.token.Token;

import java.util.Collections;
import java.util.List;

import static io.sonata.lang.javaext.Lists.append;

public class PartialGenericASTType implements ASTType {
    public final ASTType base;
    public final List<ASTType> parameters;
    public final ASTType current;

    private PartialGenericASTType(ASTType base, List<ASTType> parameters, ASTType current) {
        this.base = base;
        this.parameters = parameters;
        this.current = current;
    }

    public static PartialGenericASTType on(ASTType base) {
        return new PartialGenericASTType(base, Collections.emptyList(), EmptyASTType.instance());
    }

    @Override
    public ASTType consume(Token token) {
        ASTType next = current.consume(token);
        if (next == null) {
            if (token instanceof SeparatorToken) {
                SeparatorToken sep = (SeparatorToken) token;
                if (sep.separator.equals(",")) {
                    return new PartialGenericASTType(base, append(parameters, current), EmptyASTType.instance());
                }

                if (sep.separator.equals("]")) {
                    if (parameters.size() == 0 && current instanceof EmptyASTType) {
                        return new ArrayASTType(base);
                    }

                    return new GenericASTType(base, append(parameters, current));
                }
            }
        }

        return new PartialGenericASTType(base, parameters, next);
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
