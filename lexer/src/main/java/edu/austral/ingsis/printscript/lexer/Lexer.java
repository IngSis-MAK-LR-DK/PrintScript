package edu.austral.ingsis.printscript.lexer;

import edu.austral.ingsis.printscript.common.Token;
import java.io.Reader;
import java.util.Iterator;

/**
 * Turns PrintScript source code into a stream of {@link Token}s.
 *
 * <p>The returned iterator reads from {@code source} lazily, one token at a time, so callers
 * never need to hold an entire (potentially huge) source file in memory.
 */
public interface Lexer {

    Iterator<Token> tokenize(Reader source);
}
