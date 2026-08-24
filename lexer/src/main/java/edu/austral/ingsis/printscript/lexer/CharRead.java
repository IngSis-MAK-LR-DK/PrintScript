package edu.austral.ingsis.printscript.lexer;

public record CharRead(int codePoint, long nextOffset) {

    public static final int END_OF_INPUT = -1;

    public static CharRead endOfInput(long offset) {
        return new CharRead(END_OF_INPUT, offset);
    }

    public boolean isEndOfInput() {
        return codePoint == END_OF_INPUT;
    }
}
