package edu.austral.ingsis.printscript.interpreter;

import edu.austral.ingsis.printscript.common.ast.AssignmentStatement;
import edu.austral.ingsis.printscript.common.ast.PrintlnStatement;
import edu.austral.ingsis.printscript.common.ast.StatementVisitor;
import edu.austral.ingsis.printscript.common.ast.VariableDeclarationStatement;

/**
 * Executes one statement against a fixed {@link Environment} and returns the {@link Environment}
 * that comes out the other side. A new executor gets built for every statement, seeded with
 * whatever the previous one returned so nothing here ever needs to mutate {@code environment}
 * itself.
 */
final class StatementExecutor implements StatementVisitor<Environment> {

    private final Environment environment;
    private final ExpressionEvaluator evaluator;
    private final Emitter emitter;

    StatementExecutor(Environment environment, Emitter emitter) {
        this.environment = environment;
        this.evaluator = new ExpressionEvaluator(environment);
        this.emitter = emitter;
    }

    @Override
    public Environment visitVariableDeclaration(VariableDeclarationStatement statement) {
        Environment declared =
                environment.declare(
                        statement.identifierName(), statement.typeName(), statement.start());
        return statement
                .initializer()
                .map(initializer -> initializer.accept(evaluator))
                .map(value -> declared.assign(statement.identifierName(), value, statement.start()))
                .orElse(declared);
    }

    @Override
    public Environment visitAssignment(AssignmentStatement statement) {
        Object value = statement.value().accept(evaluator);
        return environment.assign(statement.identifierName(), value, statement.start());
    }

    @Override
    public Environment visitPrintln(PrintlnStatement statement) {
        Object value = statement.argument().accept(evaluator);
        emitter.emit(ExpressionEvaluator.stringify(value));
        return environment;
    }
}
