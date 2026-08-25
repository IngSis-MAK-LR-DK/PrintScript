package edu.austral.ingsis.printscript.common.ast;

import java.util.Optional;

import edu.austral.ingsis.printscript.common.Position;

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
