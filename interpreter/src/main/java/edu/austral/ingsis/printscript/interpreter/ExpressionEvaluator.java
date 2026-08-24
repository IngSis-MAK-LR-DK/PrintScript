package edu.austral.ingsis.printscript.interpreter;

import edu.austral.ingsis.printscript.common.SemanticException;
import edu.austral.ingsis.printscript.common.ast.BinaryExpression;
import edu.austral.ingsis.printscript.common.ast.ExpressionVisitor;
import edu.austral.ingsis.printscript.common.ast.ExtendedBinaryExpression;
import edu.austral.ingsis.printscript.common.ast.IdentifierExpression;
import edu.austral.ingsis.printscript.common.ast.NumberLiteralExpression;
import edu.austral.ingsis.printscript.common.ast.StringLiteralExpression;

/** Evaluates an {@link edu.austral.ingsis.printscript.common.ast.Expression} to a runtime value. */
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
    public Object visitIdentifier(IdentifierExpression expression) {
        return environment.read(expression.name(), expression.start());
    }

    @Override
    public Object visitBinary(BinaryExpression expression) {
        Object left = expression.left().accept(this);
        Object right = expression.right().accept(this);

        return switch (expression.operator()) {
            case PLUS -> add(left, right, expression);
            case MINUS -> arithmetic(left, right, expression, (a, b) -> a - b);
            case MULTIPLY -> arithmetic(left, right, expression, (a, b) -> a * b);
            case DIVIDE -> arithmetic(left, right, expression, (a, b) -> a / b);
        };
    }

    @Override
    public Object visitExtendedBinary(ExtendedBinaryExpression expression) {
        Object left = expression.left().accept(this);
        Object right = expression.right().accept(this);
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

    private Object add(Object left, Object right, BinaryExpression expression) {
        if (left instanceof String || right instanceof String) {
            return stringify(left) + stringify(right);
        }
        if (left instanceof Double leftNumber && right instanceof Double rightNumber) {
            return leftNumber + rightNumber;
        }
        throw typeError(expression);
    }

    private Object arithmetic(
            Object left, Object right, BinaryExpression expression, DoubleBinaryOp op) {
        if (left instanceof Double leftNumber && right instanceof Double rightNumber) {
            return op.apply(leftNumber, rightNumber);
        }
        throw typeError(expression);
    }

    private SemanticException typeError(BinaryExpression expression) {
        return new SemanticException(
                "Arithmetic operators require 'number' operands",
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

    @FunctionalInterface
    private interface DoubleBinaryOp {
        double apply(double a, double b);
    }
}
