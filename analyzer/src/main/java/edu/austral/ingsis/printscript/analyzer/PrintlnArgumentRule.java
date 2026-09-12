package edu.austral.ingsis.printscript.analyzer;

import java.util.Optional;

import edu.austral.ingsis.printscript.common.ast.BooleanLiteralExpression;
import edu.austral.ingsis.printscript.common.ast.Expression;
import edu.austral.ingsis.printscript.common.ast.IdentifierExpression;
import edu.austral.ingsis.printscript.common.ast.NumberLiteralExpression;
import edu.austral.ingsis.printscript.common.ast.PrintlnStatement;
import edu.austral.ingsis.printscript.common.ast.Statement;
import edu.austral.ingsis.printscript.common.ast.StringLiteralExpression;

/** Checks that {@code println} is only called with an identifier or a literal. */
final class PrintlnArgumentRule implements AnalysisRule {

    @Override
    public Optional<AnalysisFinding> check(Statement statement) {
        if (!(statement instanceof PrintlnStatement println)) {
            return Optional.empty();
        }
        if (isIdentifierOrLiteral(println.argument())) {
            return Optional.empty();
        }
        return Optional.of(
                new AnalysisFinding(
                        "'println' must be called with an identifier or a literal, not an expression",
                        println.argument().start(),
                        println.argument().end()));
    }

    private static boolean isIdentifierOrLiteral(Expression expression) {
        return expression instanceof IdentifierExpression
                || expression instanceof NumberLiteralExpression
                || expression instanceof StringLiteralExpression
                || expression instanceof BooleanLiteralExpression;
    }
}
