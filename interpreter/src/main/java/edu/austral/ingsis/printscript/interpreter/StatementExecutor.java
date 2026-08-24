package edu.austral.ingsis.printscript.interpreter;

import java.io.PrintStream;

import edu.austral.ingsis.printscript.common.ast.AssignmentStatement;
import edu.austral.ingsis.printscript.common.ast.PrintlnStatement;
import edu.austral.ingsis.printscript.common.ast.StatementVisitor;
import edu.austral.ingsis.printscript.common.ast.VariableDeclarationStatement;

final class StatementExecutor implements StatementVisitor<Void> {

    private final Environment environment;
    private final ExpressionEvaluator evaluator;
    private final PrintStream output;

    StatementExecutor(Environment environment, PrintStream output) {
        this.environment = environment;
        this.evaluator = new ExpressionEvaluator(environment);
        this.output = output;
    }

    @Override
    public Void visitVariableDeclaration(VariableDeclarationStatement statement) {
        environment.declare(statement.identifierName(), statement.typeName(), statement.start());
        statement
                .initializer()
                .ifPresent(
                        initializer -> {
                            Object value = initializer.accept(evaluator);
                            environment.assign(
                                    statement.identifierName(), value, statement.start());
                        });
        return null;
    }

    @Override
    public Void visitAssignment(AssignmentStatement statement) {
        Object value = statement.value().accept(evaluator);
        environment.assign(statement.identifierName(), value, statement.start());
        return null;
    }

    @Override
    public Void visitPrintln(PrintlnStatement statement) {
        Object value = statement.argument().accept(evaluator);
        output.println(ExpressionEvaluator.stringify(value));
        return null;
    }
}
