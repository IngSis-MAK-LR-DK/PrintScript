package edu.austral.ingsis.printscript.lexer;

import edu.austral.ingsis.printscript.common.TokenStream;

/**
 * Turns source code into a stream of tokens.
 *
 * <p>Tokens come out lazily, one at a time, so the whole file never has to sit in memory at once.
 */
public interface Lexer {

    TokenStream tokenize(PositionalSource source);
}
