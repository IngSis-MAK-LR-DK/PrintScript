package edu.austral.ingsis.printscript.analyzer;

import java.util.Optional;

import edu.austral.ingsis.printscript.common.ast.AssignmentStatement;
import edu.austral.ingsis.printscript.common.ast.BinaryExpression;
import edu.austral.ingsis.printscript.common.ast.BooleanLiteralExpression;
import edu.austral.ingsis.printscript.common.ast.Expression;
import edu.austral.ingsis.printscript.common.ast.IdentifierExpression;
import edu.austral.ingsis.printscript.common.ast.NumberLiteralExpression;
import edu.austral.ingsis.printscript.common.ast.PrintlnStatement;
import edu.austral.ingsis.printscript.common.ast.ReadEnvExpression;
import edu.austral.ingsis.printscript.common.ast.ReadInputExpression;
import edu.austral.ingsis.printscript.common.ast.Statement;
import edu.austral.ingsis.printscript.common.ast.StringLiteralExpression;
import edu.austral.ingsis.printscript.common.ast.VariableDeclarationStatement;

/**
 * Checks that every {@code readInput}/{@code readEnv} call is given an identifier or a literal as
 * its argument, not an arbitrary expression. Unlike {@link PrintlnArgumentRule} (whose target is
 * always the statement's single direct argument), a read call can be buried anywhere inside an
 * expression tree - inside a declaration's initializer, an assignment's value, or nested inside a
 * binary expression - so this walks the whole expression tree reachable from a statement instead of
 * only its immediate children.
 *
 * <p>Reports at most one violation per statement, the same granularity {@link AnalysisRule} already
 * has everywhere else - a statement with two separate invalid read calls only surfaces the first
 * one found by this walk (matching {@link PrintlnArgumentRule}, which has the same limitation for
 * its one argument).
 */
final class ReadArgumentRule implements AnalysisRule {

    @Override
    public Optional<AnalysisFinding> check(Statement statement) {
        Expression root = expressionOf(statement);
        return root == null ? Optional.empty() : findViolation(root);
    }

    private static Expression expressionOf(Statement statement) {
        if (statement instanceof VariableDeclarationStatement declaration) {
            return declaration.initializer().orElse(null);
        }
        if (statement instanceof AssignmentStatement assignment) {
            return assignment.value();
        }
        if (statement instanceof PrintlnStatement println) {
            return println.argument();
        }
        return null;
    }

    private static Optional<AnalysisFinding> findViolation(Expression expression) {
        if (expression instanceof ReadInputExpression readInput) {
            return checkArgument(readInput.message());
        }
        if (expression instanceof ReadEnvExpression readEnv) {
            return checkArgument(readEnv.variableName());
        }
        if (expression instanceof BinaryExpression binary) {
            Optional<AnalysisFinding> left = findViolation(binary.left());
            return left.isPresent() ? left : findViolation(binary.right());
        }
        return Optional.empty();
    }

    private static Optional<AnalysisFinding> checkArgument(Expression argument) {
        if (isIdentifierOrLiteral(argument)) {
            return Optional.empty();
        }
        return Optional.of(
                new AnalysisFinding(
                        "'readInput'/'readEnv' must be called with an identifier or a literal, not"
                                + " an expression",
                        argument.start(),
                        argument.end()));
    }

    private static boolean isIdentifierOrLiteral(Expression expression) {
        return expression instanceof IdentifierExpression
                || expression instanceof NumberLiteralExpression
                || expression instanceof StringLiteralExpression
                || expression instanceof BooleanLiteralExpression;
    }
}
