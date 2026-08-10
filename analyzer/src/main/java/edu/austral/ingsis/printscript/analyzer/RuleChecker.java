package edu.austral.ingsis.printscript.analyzer;

import edu.austral.ingsis.printscript.common.Position;
import edu.austral.ingsis.printscript.common.ast.AssignmentStatement;
import edu.austral.ingsis.printscript.common.ast.Expression;
import edu.austral.ingsis.printscript.common.ast.IdentifierExpression;
import edu.austral.ingsis.printscript.common.ast.NumberLiteralExpression;
import edu.austral.ingsis.printscript.common.ast.PrintlnStatement;
import edu.austral.ingsis.printscript.common.ast.StatementVisitor;
import edu.austral.ingsis.printscript.common.ast.StringLiteralExpression;
import edu.austral.ingsis.printscript.common.ast.VariableDeclarationStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

final class RuleChecker implements StatementVisitor<Void> {

    private static final Pattern CAMEL_CASE = Pattern.compile("^[a-z][a-zA-Z0-9]*$");
    private static final Pattern SNAKE_CASE = Pattern.compile("^[a-z][a-z0-9_]*$");

    private final AnalyzerConfig config;
    private final List<AnalysisFinding> findings = new ArrayList<>();

    RuleChecker(AnalyzerConfig config) {
        this.config = config;
    }

    List<AnalysisFinding> findings() {
        return findings;
    }

    @Override
    public Void visitVariableDeclaration(VariableDeclarationStatement statement) {
        checkIdentifierCase(statement.identifierName(), statement.start(), statement.end());
        return null;
    }

    @Override
    public Void visitAssignment(AssignmentStatement statement) {
        return null;
    }

    @Override
    public Void visitPrintln(PrintlnStatement statement) {
        if (config.printlnArgumentMustBeIdentifierOrLiteral() && !isIdentifierOrLiteral(statement.argument())) {
            findings.add(
                    new AnalysisFinding(
                            "'println' must be called with an identifier or a literal, not an expression",
                            statement.argument().start(),
                            statement.argument().end()));
        }
        return null;
    }

    private void checkIdentifierCase(String name, Position start, Position end) {
        if (!config.identifierCaseCheckEnabled()) {
            return;
        }
        Pattern pattern = config.identifierCase() == IdentifierCase.CAMEL_CASE ? CAMEL_CASE : SNAKE_CASE;
        if (!pattern.matcher(name).matches()) {
            findings.add(
                    new AnalysisFinding(
                            "Identifier '" + name + "' does not follow " + config.identifierCase() + " naming convention",
                            start,
                            end));
        }
    }

    private static boolean isIdentifierOrLiteral(Expression expression) {
        return expression instanceof IdentifierExpression
                || expression instanceof NumberLiteralExpression
                || expression instanceof StringLiteralExpression;
    }
}
