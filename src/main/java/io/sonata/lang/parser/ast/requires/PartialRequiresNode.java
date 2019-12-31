package io.sonata.lang.parser.ast.requires;

import io.sonata.lang.parser.ast.Node;
import io.sonata.lang.source.SourcePosition;
import io.sonata.lang.tokenizer.token.Token;

public class PartialRequiresNode implements Node {
    public static PartialRequiresNode initial(SourcePosition definition) {
        return new PartialRequiresNode(definition, "", State.IN_NAME);
    }

    private PartialRequiresNode(SourcePosition definition, String module, State state) {
        this.definition = definition;
        this.module = module;
        this.state = state;
    }

    enum State {
        IN_SEPARATOR, IN_NAME
    }

    private final SourcePosition definition;
    private final String module;
    private final State state;

    @Override
    public String representation() {
        return "requires " + module + " ?";
    }

    @Override
    public Node consume(Token token) {
        switch (state) {
            case IN_NAME:
                return new PartialRequiresNode(definition, module + token.representation(), State.IN_SEPARATOR);
            case IN_SEPARATOR:
                if (token.representation().equals("\n")) {
                    return new RequiresNode(definition, module);
                }

                return new PartialRequiresNode(definition, module + ".", State.IN_NAME);
        }

        return null;
    }

    @Override
    public SourcePosition definition() {
        return definition;
    }
}
