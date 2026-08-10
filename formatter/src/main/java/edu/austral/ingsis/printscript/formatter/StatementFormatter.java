package edu.austral.ingsis.printscript.formatter;

import edu.austral.ingsis.printscript.common.ast.AssignmentStatement;
import edu.austral.ingsis.printscript.common.ast.PrintlnStatement;
import edu.austral.ingsis.printscript.common.ast.StatementVisitor;
import edu.austral.ingsis.printscript.common.ast.VariableDeclarationStatement;

final class StatementFormatter implements StatementVisitor<String> {

    private final FormatterConfig config;
    private final ExpressionFormatter expressionFormatter = new ExpressionFormatter();

    StatementFormatter(FormatterConfig config) {
        this.config = config;
    }

    @Override
    public String visitVariableDeclaration(VariableDeclarationStatement statement) {
        StringBuilder text = new StringBuilder("let ").append(statement.identifierName());
        if (config.spaceBeforeColon()) {
            text.append(' ');
        }
        text.append(':');
        if (config.spaceAfterColon()) {
            text.append(' ');
        }
        text.append(statement.typeName());
        statement.initializer().ifPresent(initializer -> appendAssignedValue(text, initializer.accept(expressionFormatter)));
        return text.append(';').toString();
    }

    @Override
    public String visitAssignment(AssignmentStatement statement) {
        StringBuilder text = new StringBuilder(statement.identifierName());
        appendAssignedValue(text, statement.value().accept(expressionFormatter));
        return text.append(';').toString();
    }

    @Override
    public String visitPrintln(PrintlnStatement statement) {
        return "println(" + statement.argument().accept(expressionFormatter) + ");";
    }

    private void appendAssignedValue(StringBuilder text, String formattedValue) {
        if (config.spaceAroundEquals()) {
            text.append(' ');
        }
        text.append('=');
        if (config.spaceAroundEquals()) {
            text.append(' ');
        }
        text.append(formattedValue);
    }
}
