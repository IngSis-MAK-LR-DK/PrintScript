package edu.austral.ingsis.printscript.analyzer;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import edu.austral.ingsis.printscript.common.Position;
import edu.austral.ingsis.printscript.common.ast.AssignmentStatement;
import edu.austral.ingsis.printscript.common.ast.Expression;
import edu.austral.ingsis.printscript.common.ast.IdentifierExpression;
import edu.austral.ingsis.printscript.common.ast.NumberLiteralExpression;
import edu.austral.ingsis.printscript.common.ast.PrintlnStatement;
import edu.austral.ingsis.printscript.common.ast.StatementVisitor;
import edu.austral.ingsis.printscript.common.ast.StringLiteralExpression;
import edu.austral.ingsis.printscript.common.ast.VariableDeclarationStatement;

/**
 * Checks a single statement against the configured rules and reports the findings it produces.
 *
 * <p>Every method here is pure: given the same statement and the same (immutable) {@link
 * AnalyzerConfig}, it always returns the same list and never mutates shared state. The caller
 * ({@link PrintScriptAnalyzer}) is the only place where results get accumulated.
 */
final class RuleChecker implements StatementVisitor<List<AnalysisFinding>> {

    private static final Pattern CAMEL_CASE = Pattern.compile("^[a-z][a-zA-Z0-9]*$");
    private static final Pattern SNAKE_CASE = Pattern.compile("^[a-z][a-z0-9_]*$");

    private final AnalyzerConfig config;

    RuleChecker(AnalyzerConfig config) {
        this.config = config;
    }

    @Override
    public List<AnalysisFinding> visitVariableDeclaration(VariableDeclarationStatement statement) {
        return checkIdentifierCase(statement.identifierName(), statement.start(), statement.end())
                .map(List::of)
                .orElseGet(List::of);
    }

    @Override
    public List<AnalysisFinding> visitAssignment(AssignmentStatement statement) {
        return List.of();
    }

    @Override
    public List<AnalysisFinding> visitPrintln(PrintlnStatement statement) {
        if (config.printlnArgumentMustBeIdentifierOrLiteral()
                && !isIdentifierOrLiteral(statement.argument())) {
            return List.of(
                    new AnalysisFinding(
                            "'println' must be called with an identifier or a literal, not an expression",
                            statement.argument().start(),
                            statement.argument().end()));
        }
        return List.of();
    }

    private Optional<AnalysisFinding> checkIdentifierCase(
            String name, Position start, Position end) {
        if (!config.identifierCaseCheckEnabled()) {
            return Optional.empty();
        }
        Pattern pattern =
                config.identifierCase() == IdentifierCase.CAMEL_CASE ? CAMEL_CASE : SNAKE_CASE;
        if (pattern.matcher(name).matches()) {
            return Optional.empty();
        }
        return Optional.of(
                new AnalysisFinding(
                        "Identifier '"
                                + name
                                + "' does not follow "
                                + config.identifierCase()
                                + " naming convention",
                        start,
                        end));
    }

    private static boolean isIdentifierOrLiteral(Expression expression) {
        return expression instanceof IdentifierExpression
                || expression instanceof NumberLiteralExpression
                || expression instanceof StringLiteralExpression;
    }
}
