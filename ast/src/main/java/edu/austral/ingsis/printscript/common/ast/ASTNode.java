package edu.austral.ingsis.printscript.common.ast;

import edu.austral.ingsis.printscript.common.Position;
import edu.austral.ingsis.printscript.common.SourceRange;

/** Base type for every node in the AST — statements and expressions both carry a position. */
public sealed interface ASTNode permits Statement, Expression {
    Position start();

    Position end();

    default SourceRange range() {
        return new SourceRange(start(), end());
    }
}
