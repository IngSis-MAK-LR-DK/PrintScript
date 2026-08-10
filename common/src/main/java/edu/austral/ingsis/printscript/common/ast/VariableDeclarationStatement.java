package edu.austral.ingsis.printscript.common.ast;

import edu.austral.ingsis.printscript.common.Position;
import java.util.Optional;

/** {@code let <identifierName>: <typeName> [= <initializer>];} */
public record VariableDeclarationStatement(
        String identifierName,
        String typeName,
        Optional<Expression> initializer,
        Position start,
        Position end)
        implements Statement {

    @Override
    public <R> R accept(StatementVisitor<R> visitor) {
        return visitor.visitVariableDeclaration(this);
    }
}
