package edu.austral.ingsis.printscript.interpreter;

import java.util.List;

import edu.austral.ingsis.printscript.common.SemanticException;
import edu.austral.ingsis.printscript.common.SourceRange;
import edu.austral.ingsis.printscript.common.ast.AssignmentStatement;
import edu.austral.ingsis.printscript.common.ast.Expression;
import edu.austral.ingsis.printscript.common.ast.IfStatement;
import edu.austral.ingsis.printscript.common.ast.PrintlnStatement;
import edu.austral.ingsis.printscript.common.ast.ReadEnvExpression;
import edu.austral.ingsis.printscript.common.ast.ReadInputExpression;
import edu.austral.ingsis.printscript.common.ast.Statement;
import edu.austral.ingsis.printscript.common.ast.StatementVisitor;
import edu.austral.ingsis.printscript.common.ast.VariableDeclarationStatement;

/** Checks one statement against a {@link TypeScope}, updating the scope as it goes. */
final class StatementTypeChecker implements StatementVisitor<Void> {

    private final TypeScope scope;
    private final ExpressionTypeChecker expressions;

    StatementTypeChecker(TypeScope scope) {
        this.scope = scope;
        this.expressions = new ExpressionTypeChecker(scope);
    }

    @Override
    public Void visitVariableDeclaration(VariableDeclarationStatement statement) {
        if (statement.isConstant() && statement.initializer().isEmpty()) {
            throw new SemanticException(
                    "'const' variable '" + statement.identifierName() + "' must be initialized",
                    statement.start(),
                    statement.end());
        }
        scope.declare(
                statement.identifierName(),
                statement.typeName(),
                statement.isConstant(),
                statement.identifierRange(),
                statement.typeRange());
        statement
                .initializer()
                .ifPresent(
                        initializer -> {
                            checkAssignable(
                                    statement.identifierName(), statement.typeName(), initializer);
                            scope.markAssigned(
                                    statement.identifierName(), statement.identifierRange());
                        });
        return null;
    }

    @Override
    public Void visitAssignment(AssignmentStatement statement) {
        SourceRange nameAt = statement.identifierRange();
        String declaredType = scope.typeOf(statement.identifierName(), nameAt);
        checkAssignable(statement.identifierName(), declaredType, statement.value());
        scope.markAssigned(statement.identifierName(), nameAt);
        return null;
    }

    @Override
    public Void visitPrintln(PrintlnStatement statement) {
        statement.argument().accept(expressions);
        return null;
    }

    /** Both branches get checked, whichever one would actually run. */
    @Override
    public Void visitIf(IfStatement statement) {
        String conditionType =
                scope.typeForReading(statement.condition().name(), statement.condition().range());
        if (!conditionType.equals("boolean") && !conditionType.equals(TypeScope.UNKNOWN_TYPE)) {
            throw new SemanticException(
                    "'if' condition must be a 'boolean' variable, but '"
                            + statement.condition().name()
                            + "' is not",
                    statement.condition().start(),
                    statement.condition().end());
        }
        TypeScope thenScope = checkedBranch(statement.thenBranch());
        TypeScope elseScope = checkedBranch(statement.elseBranch().orElse(List.of()));
        scope.mergedWith(thenScope, elseScope);
        return null;
    }

    private TypeScope checkedBranch(List<Statement> branch) {
        TypeScope branchScope = scope.copy();
        StatementTypeChecker checker = new StatementTypeChecker(branchScope);
        branch.forEach(statement -> statement.accept(checker));
        return branchScope;
    }

    private void checkAssignable(String name, String declaredType, Expression value) {
        String valueType = value.accept(expressions);
        // What readInput/readEnv return is converted to the declared type when the program runs.
        boolean isExternalValue =
                value instanceof ReadInputExpression || value instanceof ReadEnvExpression;
        if (isExternalValue
                || declaredType.equals(valueType)
                || declaredType.equals(TypeScope.UNKNOWN_TYPE)
                || valueType.equals(TypeScope.UNKNOWN_TYPE)) {
            return;
        }
        throw new SemanticException(
                "Cannot assign a value of type '"
                        + valueType
                        + "' to variable '"
                        + name
                        + "' of type '"
                        + declaredType
                        + "'",
                value.start(),
                value.end());
    }
}
