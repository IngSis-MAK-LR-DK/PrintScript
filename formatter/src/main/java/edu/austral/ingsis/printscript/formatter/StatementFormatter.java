package edu.austral.ingsis.printscript.formatter;

import java.util.List;

import edu.austral.ingsis.printscript.common.ast.AssignmentStatement;
import edu.austral.ingsis.printscript.common.ast.IfStatement;
import edu.austral.ingsis.printscript.common.ast.PrintlnStatement;
import edu.austral.ingsis.printscript.common.ast.Statement;
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
        StringBuilder text =
                new StringBuilder(statement.isConstant() ? "const " : "let ")
                        .append(statement.identifierName());
        if (config.spaceBeforeColon()) {
            text.append(' ');
        }
        text.append(':');
        if (config.spaceAfterColon()) {
            text.append(' ');
        }
        text.append(statement.typeName());
        statement
                .initializer()
                .ifPresent(
                        initializer ->
                                appendAssignedValue(text, initializer.accept(expressionFormatter)));
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
        String argument = statement.argument().accept(expressionFormatter);
        return config.spaceAroundParens()
                ? "println ( " + argument + " );"
                : "println(" + argument + ");";
    }

    @Override
    public String visitIf(IfStatement statement) {
        String braceOpener = config.ifBraceBelowLine() ? "\n{\n" : " {\n";
        StringBuilder text =
                new StringBuilder("if (")
                        .append(statement.condition().name())
                        .append(')')
                        .append(braceOpener);
        appendIndentedBlock(text, statement.thenBranch());
        text.append('}');
        statement
                .elseBranch()
                .ifPresent(
                        elseBranch -> {
                            text.append(config.ifBraceBelowLine() ? "\nelse" : " else")
                                    .append(braceOpener);
                            appendIndentedBlock(text, elseBranch);
                            text.append('}');
                        });
        return text.toString();
    }

    private void appendIndentedBlock(StringBuilder text, List<Statement> block) {
        String indent = " ".repeat(config.indentSize());
        for (Statement statement : block) {
            String rendered = statement.accept(this);
            for (String line : rendered.split("\n", -1)) {
                if (!line.isEmpty()) {
                    text.append(indent).append(line);
                }
                text.append('\n');
            }
        }
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
