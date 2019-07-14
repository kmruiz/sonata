package io.sonata.lang.tokenizer;

import io.reactivex.Flowable;
import io.sonata.lang.source.SourceCharacter;
import io.sonata.lang.tokenizer.token.RootToken;
import io.sonata.lang.tokenizer.token.Token;

public final class Tokenizer {
    private Token token;

    public Tokenizer() {
        this.token = RootToken.instance();
    }

    public Flowable<Token> process(SourceCharacter character) {
        var nextToken = token.nextToken(character.character);
        if (nextToken.isPresent()) {
            token = nextToken.get();
            return Flowable.empty();
        } else {
            Token toSend = token;
            token = RootToken.instance().nextToken(character.character).orElse(RootToken.instance());
            return Flowable.fromArray(toSend);
        }
    }
}
