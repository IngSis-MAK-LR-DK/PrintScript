package edu.austral.ingsis.printscript.lexer;

/**
 * A pure, random-access view over source characters: reading the same {@code offset} always returns
 * the same result (assuming the underlying data isn't modified mid-read — the same assumption any
 * compiler makes about the file it's compiling). Callers never need to remember "how far they've
 * read" via mutable state of their own; they just thread the returned offset forward.
 */
public interface PositionalSource {

    CharRead readAt(long offset);
}
