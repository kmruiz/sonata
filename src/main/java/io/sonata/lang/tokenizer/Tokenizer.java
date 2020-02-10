/*
 * Copyright (c) 2020 Kevin Mas Ruiz
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package io.sonata.lang.tokenizer;

import io.reactivex.rxjava3.core.Flowable;
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
            return Flowable.just(toSend);
        }
    }
}
