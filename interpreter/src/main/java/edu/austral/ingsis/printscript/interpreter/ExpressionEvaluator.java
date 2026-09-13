package edu.austral.ingsis.printscript.interpreter;

import edu.austral.ingsis.printscript.common.CoreOperators;
import edu.austral.ingsis.printscript.common.SemanticException;
import edu.austral.ingsis.printscript.common.ast.BinaryExpression;
import edu.austral.ingsis.printscript.common.ast.BooleanLiteralExpression;
import edu.austral.ingsis.printscript.common.ast.ExpressionVisitor;
import edu.austral.ingsis.printscript.common.ast.IdentifierExpression;
import edu.austral.ingsis.printscript.common.ast.NumberLiteralExpression;
import edu.austral.ingsis.printscript.common.ast.StringLiteralExpression;

/** Walks an expression and computes its runtime value. */
final class ExpressionEvaluator implements ExpressionVisitor<Object> {

    private final Environment environment;

    ExpressionEvaluator(Environment environment) {
        this.environment = environment;
    }

    @Override
    public Object visitNumberLiteral(NumberLiteralExpression expression) {
        return expression.value();
    }

    @Override
    public Object visitStringLiteral(StringLiteralExpression expression) {
        return expression.value();
    }

    @Override
    public Object visitBooleanLiteral(BooleanLiteralExpression expression) {
        return expression.value();
    }

    @Override
    public Object visitIdentifier(IdentifierExpression expression) {
        return environment.read(expression.name(), expression.start());
    }

    @Override
    public Object visitBinary(BinaryExpression expression) {
        Object left = expression.left().accept(this);
        Object right = expression.right().accept(this);

        // '+' also concatenates strings — an overload the OperatorDefinition#apply(double, double)
        // contract can't express (it's number-only), so it's handled here rather than inside
        // CoreOperators.PLUS itself. Every other operator, core or plugin, is number-only.
        if (expression.operator() == CoreOperators.PLUS
                && (left instanceof String || right instanceof String)) {
            return stringify(left) + stringify(right);
        }
        if (left instanceof Double leftNumber && right instanceof Double rightNumber) {
            return expression.operator().apply(leftNumber, rightNumber);
        }
        throw new SemanticException(
                "Operator '"
                        + expression.operator().symbol()
                        + "' requires operands of type number",
                expression.start(),
                expression.end());
    }

    static String stringify(Object value) {
        if (value instanceof Double number) {
            if (number == Math.floor(number) && !Double.isInfinite(number)) {
                return String.valueOf(number.longValue());
            }
            return String.valueOf(number);
        }
        return String.valueOf(value);
    }
}
