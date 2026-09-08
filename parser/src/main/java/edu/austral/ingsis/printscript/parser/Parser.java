package edu.austral.ingsis.printscript.parser;

import java.util.Iterator;

import edu.austral.ingsis.printscript.common.TokenStream;
import edu.austral.ingsis.printscript.common.ast.Statement;

/**
 * Builds the PrintScript AST out of a token stream.
 *
 * <p>The returned iterator pulls tokens lazily and yields one {@link Statement} per call to {@code
 * next()}. A statement, terminated by {@code ;}, is the natural place to stop — neither the full
 * token list nor the full AST ever needs to sit in memory at once.
 */
public interface Parser {

    Iterator<Statement> parse(TokenStream tokens);
}
