package edu.austral.ingsis.printscript.common.ast;

import java.util.Optional;

import edu.austral.ingsis.printscript.common.Position;

/** {@code (let|const) <identifierName>: <typeName> [= <initializer>];} */
public record VariableDeclarationStatement(
        String identifierName,
        String typeName,
        boolean isConstant,
        Optional<Expression> initializer,
        Position start,
        Position end)
        implements Statement {

    /** Default constructor to a non-constant declaration. */
    public VariableDeclarationStatement(
            String identifierName,
            String typeName,
            Optional<Expression> initializer,
            Position start,
            Position end) {
        this(identifierName, typeName, false, initializer, start, end);
    }

    @Override
    public <R> R accept(StatementVisitor<R> visitor) {
        return visitor.visitVariableDeclaration(this);
    }
}
