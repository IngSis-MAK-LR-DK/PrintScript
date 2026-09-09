package edu.austral.ingsis.printscript.lexer;

/**
 * A view over source characters that can be read at any offset, any number of times, and always
 * gives back the same answer. Nothing here tracks "how far we've read" — whoever calls this just
 * passes the offset along themselves.
 */
public interface PositionalSource {

    CharRead readAt(long offset);
}
