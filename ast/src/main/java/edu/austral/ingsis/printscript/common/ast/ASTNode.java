package edu.austral.ingsis.printscript.common.ast;

import edu.austral.ingsis.printscript.common.Position;

/** Base type for every node in the AST — statements and expressions both carry a position. */
public sealed interface ASTNode permits Statement, Expression {
    Position start();

    Position end();
}
