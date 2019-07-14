package io.sonata.lang.source;

public class SourceCharacter {
    public final SourcePosition position;
    public final char character;

    public SourceCharacter(SourcePosition position, char character) {
        this.position = position;
        this.character = character;
    }

    @Override
    public String toString() {
        return String.format("%s: %c", position, character);
    }
}
