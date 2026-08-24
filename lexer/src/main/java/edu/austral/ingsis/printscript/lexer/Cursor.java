package edu.austral.ingsis.printscript.lexer;

import edu.austral.ingsis.printscript.common.Position;

record Cursor(long offset, int line, int column) {

    static Cursor start() {
        return new Cursor(0, 1, 1);
    }

    Position position() {
        return new Position(line, column);
    }

    Cursor advancedOver(CharRead read) {
        if (read.isEndOfInput()) {
            return this;
        }
        if (read.codePoint() == '\n') {
            return new Cursor(read.nextOffset(), line + 1, 1);
        }
        return new Cursor(read.nextOffset(), line, column + 1);
    }
}
