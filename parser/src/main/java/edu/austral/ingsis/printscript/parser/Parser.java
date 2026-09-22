package edu.austral.ingsis.printscript.parser;

import java.util.Iterator;

import edu.austral.ingsis.printscript.common.TokenStream;
import edu.austral.ingsis.printscript.common.ast.Statement;

/**
 * Builds the PrintScript AST out of a token stream.
 *
 * <p>The returned iterator pulls tokens lazily and yields one {@link Statement} per call to {@code
 * next()}.
 */
public interface Parser {

    Iterator<Statement> parse(TokenStream tokens);
}
