package edu.austral.ingsis.printscript.lexer;

import edu.austral.ingsis.printscript.common.TokenStream;

/**
 * Turns PrintScript source code into a stream of tokens.
 *
 * <p>The returned {@link TokenStream} reads from {@code source} lazily, one token at a time, so
 * callers never need to hold an entire (potentially huge) source file in memory.
 */
public interface Lexer {

    TokenStream tokenize(PositionalSource source);
}
