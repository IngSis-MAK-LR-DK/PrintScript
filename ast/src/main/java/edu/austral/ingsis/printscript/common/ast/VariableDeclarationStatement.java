package edu.austral.ingsis.printscript.common.ast;

import java.util.Optional;

import edu.austral.ingsis.printscript.common.Position;
import edu.austral.ingsis.printscript.common.SourceRange;

/**
 * {@code (let|const) <identifierName>: <typeName> [= <initializer>];}
 *
 * @param identifierRange where the variable's name sits, so a problem with the name can point at
 *     the name and not at the whole statement
 * @param typeRange where the type's name sits, for the same reason
 */
public record VariableDeclarationStatement(
        String identifierName,
        String typeName,
        boolean isConstant,
        Optional<Expression> initializer,
        SourceRange identifierRange,
        SourceRange typeRange,
        Position start,
        Position end)
        implements Statement {

    /** For callers that have no finer location than the statement itself. */
    public VariableDeclarationStatement(
            String identifierName,
            String typeName,
            boolean isConstant,
            Optional<Expression> initializer,
            Position start,
            Position end) {
        this(
                identifierName,
                typeName,
                isConstant,
                initializer,
                new SourceRange(start, end),
                new SourceRange(start, end),
                start,
                end);
    }

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
