package io.sonata.lang.tokenizer;

import io.reactivex.Flowable;
import io.sonata.lang.source.SourceCharacter;
import io.sonata.lang.tokenizer.token.RootToken;
import io.sonata.lang.tokenizer.token.Token;

import java.util.Optional;

public final class Tokenizer {
    private Token token;

    public Tokenizer() {
        this.token = RootToken.instance();
    }

    public Flowable<Token> process(SourceCharacter character) {
        Optional<Token> nextToken = token.nextToken(character);
        if (nextToken.isPresent()) {
            token = nextToken.get();
            return Flowable.empty();
        } else {
            Token toSend = token;
            token = RootToken.instance().nextToken(character).orElse(RootToken.instance());
            return Flowable.fromArray(toSend);
        }
    }
}
