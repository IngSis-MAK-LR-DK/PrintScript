package edu.austral.ingsis.printscript.common;

/** Where something begins and ends in a source file, e.g. the name inside a declaration. */
public record SourceRange(Position start, Position end) {}
