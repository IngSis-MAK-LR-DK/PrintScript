package edu.austral.ingsis.printscript.lexer;

public final class StringPositionalSource implements PositionalSource {

    private final String content;

    public StringPositionalSource(String content) {
        this.content = content;
    }

    @Override
    public CharRead readAt(long offset) {
        int index = (int) offset;
        if (index >= content.length()) {
            return CharRead.endOfInput(offset);
        }
        int codePoint = content.codePointAt(index);
        return new CharRead(codePoint, offset + Character.charCount(codePoint));
    }
}
