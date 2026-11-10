package edu.austral.ingsis.printscript.interpreter;

import edu.austral.ingsis.printscript.common.CoreOperators;
import edu.austral.ingsis.printscript.common.SemanticException;
import edu.austral.ingsis.printscript.common.ast.BinaryExpression;
import edu.austral.ingsis.printscript.common.ast.BooleanLiteralExpression;
import edu.austral.ingsis.printscript.common.ast.ExpressionVisitor;
import edu.austral.ingsis.printscript.common.ast.IdentifierExpression;
import edu.austral.ingsis.printscript.common.ast.NumberLiteralExpression;
import edu.austral.ingsis.printscript.common.ast.ReadEnvExpression;
import edu.austral.ingsis.printscript.common.ast.ReadInputExpression;
import edu.austral.ingsis.printscript.common.ast.StringLiteralExpression;

/** Works out the type an expression would have at runtime, without evaluating anything. */
final class ExpressionTypeChecker implements ExpressionVisitor<String> {

    private static final String NUMBER = "number";
    private static final String STRING = "string";
    private static final String BOOLEAN = "boolean";

    private final TypeScope scope;

    ExpressionTypeChecker(TypeScope scope) {
        this.scope = scope;
    }

    @Override
    public String visitNumberLiteral(NumberLiteralExpression expression) {
        return NUMBER;
    }

    @Override
    public String visitStringLiteral(StringLiteralExpression expression) {
        return STRING;
    }

    @Override
    public String visitBooleanLiteral(BooleanLiteralExpression expression) {
        return BOOLEAN;
    }

    @Override
    public String visitIdentifier(IdentifierExpression expression) {
        return scope.typeForReading(expression.name(), expression.range());
    }

    @Override
    public String visitBinary(BinaryExpression expression) {
        String left = expression.left().accept(this);
        String right = expression.right().accept(this);

        if (expression.operator() == CoreOperators.PLUS
                && (left.equals(STRING) || right.equals(STRING))) {
            return STRING;
        }
        if (left.equals(TypeScope.UNKNOWN_TYPE) || right.equals(TypeScope.UNKNOWN_TYPE)) {
            return TypeScope.UNKNOWN_TYPE;
        }
        if (left.equals(NUMBER) && right.equals(NUMBER)) {
            return NUMBER;
        }
        throw new SemanticException(
                "Operator '"
                        + expression.operator().symbol()
                        + "' requires operands of type number",
                expression.start(),
                expression.end());
    }

    @Override
    public String visitReadInput(ReadInputExpression expression) {
        expression.message().accept(this);
        return STRING;
    }

    @Override
    public String visitReadEnv(ReadEnvExpression expression) {
        expression.variableName().accept(this);
        return STRING;
    }
}
