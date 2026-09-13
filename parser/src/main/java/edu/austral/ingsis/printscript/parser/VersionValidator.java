package edu.austral.ingsis.printscript.parser;

import java.util.List;

import edu.austral.ingsis.printscript.common.Position;
import edu.austral.ingsis.printscript.common.SyntaxException;
import edu.austral.ingsis.printscript.common.Version;
import edu.austral.ingsis.printscript.common.ast.AssignmentStatement;
import edu.austral.ingsis.printscript.common.ast.BinaryExpression;
import edu.austral.ingsis.printscript.common.ast.BooleanLiteralExpression;
import edu.austral.ingsis.printscript.common.ast.ExpressionVisitor;
import edu.austral.ingsis.printscript.common.ast.IdentifierExpression;
import edu.austral.ingsis.printscript.common.ast.NumberLiteralExpression;
import edu.austral.ingsis.printscript.common.ast.PrintlnStatement;
import edu.austral.ingsis.printscript.common.ast.Statement;
import edu.austral.ingsis.printscript.common.ast.StatementVisitor;
import edu.austral.ingsis.printscript.common.ast.StringLiteralExpression;
import edu.austral.ingsis.printscript.common.ast.VariableDeclarationStatement;

/**
 * Walks an already-parsed AST and rejects any construct the active {@link Version} doesn't support
 * yet. {@link StatementIterator} always accepts the full grammar across every version — this is the
 * single place version policy lives, so a new version-gated construct is one visitor method here,
 * and the compiler enforces every statement/expression kind is accounted for.
 *
 * <p>The one thing this can't cover: a type <em>name</em> like {@code "boolean"} is just a {@code
 * String} field on {@link VariableDeclarationStatement}, not its own node kind, so there's no
 * visitor method the compiler can force for it — it stays a targeted, explicit check inside {@link
 * #visitVariableDeclaration}, same as it was when this lived in the parser.
 */
final class VersionValidator implements StatementVisitor<Void>, ExpressionVisitor<Void> {

    private final Version version;

    private VersionValidator(Version version) {
        this.version = version;
    }

    static void validate(List<Statement> statements, Version version) {
        VersionValidator validator = new VersionValidator(version);
        for (Statement statement : statements) {
            statement.accept(validator);
        }
    }

    @Override
    public Void visitVariableDeclaration(VariableDeclarationStatement statement) {
        if (statement.typeName().equals("boolean")) {
            requireVersion(Version.V1_1, "The 'boolean' type", statement.start(), statement.end());
        }
        statement.initializer().ifPresent(initializer -> initializer.accept(this));
        return null;
    }

    @Override
    public Void visitAssignment(AssignmentStatement statement) {
        statement.value().accept(this);
        return null;
    }

    @Override
    public Void visitPrintln(PrintlnStatement statement) {
        statement.argument().accept(this);
        return null;
    }

    @Override
    public Void visitNumberLiteral(NumberLiteralExpression expression) {
        return null;
    }

    @Override
    public Void visitStringLiteral(StringLiteralExpression expression) {
        return null;
    }

    @Override
    public Void visitBooleanLiteral(BooleanLiteralExpression expression) {
        requireVersion(Version.V1_1, "Boolean literals", expression.start(), expression.end());
        return null;
    }

    @Override
    public Void visitIdentifier(IdentifierExpression expression) {
        return null;
    }

    @Override
    public Void visitBinary(BinaryExpression expression) {
        expression.left().accept(this);
        expression.right().accept(this);
        return null;
    }

    private void requireVersion(Version required, String feature, Position start, Position end) {
        if (!version.isAtLeast(required)) {
            throw new SyntaxException(
                    feature
                            + ": requires PrintScript "
                            + required.label()
                            + " or later (running "
                            + version.label()
                            + ")",
                    start,
                    end);
        }
    }
}
