package edu.austral.ingsis.printscript.parser;

import java.util.Iterator;

import edu.austral.ingsis.printscript.common.Position;
import edu.austral.ingsis.printscript.common.SyntaxException;
import edu.austral.ingsis.printscript.common.Version;
import edu.austral.ingsis.printscript.common.ast.AssignmentStatement;
import edu.austral.ingsis.printscript.common.ast.BinaryExpression;
import edu.austral.ingsis.printscript.common.ast.BooleanLiteralExpression;
import edu.austral.ingsis.printscript.common.ast.ExpressionVisitor;
import edu.austral.ingsis.printscript.common.ast.IdentifierExpression;
import edu.austral.ingsis.printscript.common.ast.IfStatement;
import edu.austral.ingsis.printscript.common.ast.NumberLiteralExpression;
import edu.austral.ingsis.printscript.common.ast.PrintlnStatement;
import edu.austral.ingsis.printscript.common.ast.ReadEnvExpression;
import edu.austral.ingsis.printscript.common.ast.ReadInputExpression;
import edu.austral.ingsis.printscript.common.ast.Statement;
import edu.austral.ingsis.printscript.common.ast.StatementVisitor;
import edu.austral.ingsis.printscript.common.ast.StringLiteralExpression;
import edu.austral.ingsis.printscript.common.ast.VariableDeclarationStatement;

/**
 * Rejects any construct the active {@link Version} doesn't support yet. {@link StatementIterator}
 * always accepts the full grammar across every version — this is the single place version policy
 * lives, so a new version-gated construct is one visitor method here, and the compiler enforces
 * every statement/expression kind is accounted for.
 *
 * <p>{@link #validating} wraps the parser's statement iterator lazily: each statement is checked
 * exactly when it's pulled via {@code next()}, never buffered or checked up front. That matters for
 * two reasons - it keeps this a single-pass, streaming pipeline (nothing here ever holds the whole
 * program in memory, same as {@code TokenStream}), and it means a version violation surfaces at the
 * same point any other error in this language already does: per statement, as execution/formatting
 * reaches it, not through a separate whole-program pass. The trade-off that comes with that: a
 * violation later in the program doesn't stop earlier, valid statements from being handed back
 * (and, in {@code execution} mode, actually running) first - there's no "reject the whole file up
 * front" guarantee. That's judged to be the right trade for this language over buffering the full
 * AST just to get it.
 *
 * <p>The one thing this can't cover: a type <em>name</em> like {@code "boolean"} is just a {@code
 * String} field on {@link VariableDeclarationStatement}, not its own node kind, so there's no
 * visitor method the compiler can force for it — it stays a targeted, explicit check inside {@link
 * #visitVariableDeclaration}, same as it was when this lived in the parser.
 */
final class VersionValidator implements StatementVisitor<Void>, ExpressionVisitor<Void> {

    private final Version version;

    VersionValidator(Version version) {
        this.version = version;
    }

    Iterator<Statement> validating(Iterator<Statement> statements) {
        return new Iterator<>() {
            @Override
            public boolean hasNext() {
                return statements.hasNext();
            }

            @Override
            public Statement next() {
                Statement statement = statements.next();
                statement.accept(VersionValidator.this);
                return statement;
            }
        };
    }

    @Override
    public Void visitVariableDeclaration(VariableDeclarationStatement statement) {
        if (statement.typeName().equals("boolean")) {
            requireVersion(Version.V1_1, "The 'boolean' type", statement.start(), statement.end());
        }
        if (statement.isConstant()) {
            requireVersion(Version.V1_1, "'const'", statement.start(), statement.end());
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
    public Void visitIf(IfStatement statement) {
        requireVersion(Version.V1_1, "'if'/'else'", statement.start(), statement.end());
        statement.thenBranch().forEach(s -> s.accept(this));
        statement.elseBranch().ifPresent(branch -> branch.forEach(s -> s.accept(this)));
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

    @Override
    public Void visitReadInput(ReadInputExpression expression) {
        requireVersion(Version.V1_1, "'readInput'", expression.start(), expression.end());
        expression.message().accept(this);
        return null;
    }

    @Override
    public Void visitReadEnv(ReadEnvExpression expression) {
        requireVersion(Version.V1_1, "'readEnv'", expression.start(), expression.end());
        expression.variableName().accept(this);
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
