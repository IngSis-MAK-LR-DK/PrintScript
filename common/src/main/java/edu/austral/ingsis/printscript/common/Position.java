package edu.austral.ingsis.printscript.common;

/** A 1-based line/column location within a source file. */
public record Position(int line, int column) {

    public static final Position UNKNOWN = new Position(-1, -1);
}
