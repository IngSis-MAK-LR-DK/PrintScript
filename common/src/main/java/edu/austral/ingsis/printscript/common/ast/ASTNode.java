package edu.austral.ingsis.printscript.common.ast;

import edu.austral.ingsis.printscript.common.Position;

/** Common contract for every node of the PrintScript AST. */
public sealed interface ASTNode permits Statement, Expression {

    Position start();

    Position end();
}
