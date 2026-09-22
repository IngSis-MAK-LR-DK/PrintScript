package edu.austral.ingsis.printscript.interpreter;

import java.util.List;

import edu.austral.ingsis.printscript.common.SemanticException;
import edu.austral.ingsis.printscript.common.ast.AssignmentStatement;
import edu.austral.ingsis.printscript.common.ast.Expression;
import edu.austral.ingsis.printscript.common.ast.IfStatement;
import edu.austral.ingsis.printscript.common.ast.PrintlnStatement;
import edu.austral.ingsis.printscript.common.ast.ReadEnvExpression;
import edu.austral.ingsis.printscript.common.ast.ReadInputExpression;
import edu.austral.ingsis.printscript.common.ast.Statement;
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
    private final ExecutionContext context;

    StatementExecutor(Environment environment, ExecutionContext context) {
        this.environment = environment;
        this.evaluator =
                new ExpressionEvaluator(
                        environment, context.inputProvider(), context.environmentReader());
        this.context = context;
    }

    @Override
    public Environment visitVariableDeclaration(VariableDeclarationStatement statement) {
        if (statement.isConstant() && statement.initializer().isEmpty()) {
            throw new SemanticException(
                    "'const' variable '" + statement.identifierName() + "' must be initialized",
                    statement.start(),
                    statement.end());
        }
        Environment declared =
                environment.declare(
                        statement.identifierName(),
                        statement.typeName(),
                        statement.isConstant(),
                        statement.start());
        return statement
                .initializer()
                .map(initializer -> evaluateForAssignment(initializer, statement.typeName()))
                .map(value -> declared.assign(statement.identifierName(), value, statement.start()))
                .orElse(declared);
    }

    @Override
    public Environment visitAssignment(AssignmentStatement statement) {
        String declaredType = environment.typeOf(statement.identifierName(), statement.start());
        Object value = evaluateForAssignment(statement.value(), declaredType);
        return environment.assign(statement.identifierName(), value, statement.start());
    }

    @Override
    public Environment visitPrintln(PrintlnStatement statement) {
        Object value = statement.argument().accept(evaluator);
        context.emitter().emit(ExpressionEvaluator.stringify(value));
        return environment;
    }

    @Override
    public Environment visitIf(IfStatement statement) {
        Object conditionValue =
                environment.read(statement.condition().name(), statement.condition().start());
        if (!(conditionValue instanceof Boolean isTrue)) {
            throw new SemanticException(
                    "'if' condition must be a 'boolean' variable, but '"
                            + statement.condition().name()
                            + "' is not",
                    statement.condition().start(),
                    statement.condition().end());
        }
        List<Statement> branch =
                isTrue ? statement.thenBranch() : statement.elseBranch().orElse(List.of());
        Environment current = environment;
        for (Statement inner : branch) {
            current = inner.accept(new StatementExecutor(current, context));
        }
        return current;
    }

    /**
     * Evaluates {@code expression} for use as the value of a declaration/assignment. {@code
     * readInput}/{@code readEnv} always evaluate to a raw {@code String}.
     */
    private Object evaluateForAssignment(Expression expression, String declaredType) {
        Object raw = expression.accept(evaluator);
        if (expression instanceof ReadInputExpression || expression instanceof ReadEnvExpression) {
            return RuntimeValueCoercer.coerce((String) raw, declaredType, expression);
        }
        return raw;
    }
}
