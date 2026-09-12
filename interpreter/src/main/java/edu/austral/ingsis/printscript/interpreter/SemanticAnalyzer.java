package edu.austral.ingsis.printscript.interpreter;

import java.util.Iterator;

import edu.austral.ingsis.printscript.common.ast.Statement;

/**
 * Confirms a program is semantically valid — every variable declared before use, every assignment
 * type-matched — without the caller needing to know or care how that's checked. Throws {@code
 * SemanticException} on the first problem found.
 */
public interface SemanticAnalyzer {

    void analyze(Iterator<Statement> statements);
}
