package edu.austral.ingsis.printscript.parser;

import edu.austral.ingsis.printscript.common.TokenStream;
import edu.austral.ingsis.printscript.common.ast.Statement;
import java.util.Iterator;

/**
 * Builds the PrintScript AST out of a token stream.
 *
 * <p>The returned iterator pulls tokens from {@code tokens} lazily and yields one {@link
 * Statement} per call to {@code next()} — a statement (terminated by {@code ;}) is the natural
 * streaming unit, so neither the full token list nor the full AST ever need to be in memory at
 * once.
 */
public interface Parser {

    Iterator<Statement> parse(TokenStream tokens);
}
