package edu.austral.ingsis.printscript.formatter;

import java.util.Map;

import edu.austral.ingsis.printscript.common.OperatorDefinition;
import edu.austral.ingsis.printscript.common.ast.BinaryExpression;
import edu.austral.ingsis.printscript.common.ast.BooleanLiteralExpression;
import edu.austral.ingsis.printscript.common.ast.Expression;
import edu.austral.ingsis.printscript.common.ast.ExpressionVisitor;
import edu.austral.ingsis.printscript.common.ast.IdentifierExpression;
import edu.austral.ingsis.printscript.common.ast.NumberLiteralExpression;
import edu.austral.ingsis.printscript.common.ast.ReadEnvExpression;
import edu.austral.ingsis.printscript.common.ast.ReadInputExpression;
import edu.austral.ingsis.printscript.common.ast.StringLiteralExpression;

/**
 * Renders an expression to text. Operators always get one space on each side.
 *
 * <p>The AST doesn't keep the source's parentheses, so they're put back only where precedence needs
 * them for the output to parse into the same tree again — every operator is left-associative, so a
 * right operand at the same level as its parent keeps them too ({@code a - (b - c)}).
 */
final class ExpressionFormatter implements ExpressionVisitor<String> {

    private final Map<OperatorDefinition, Integer> precedenceLevels;

    ExpressionFormatter(Map<OperatorDefinition, Integer> precedenceLevels) {
        this.precedenceLevels = precedenceLevels;
    }

    @Override
    public String visitNumberLiteral(NumberLiteralExpression expression) {
        double value = expression.value();
        if (value == Math.floor(value) && !Double.isInfinite(value)) {
            return String.valueOf((long) value);
        }
        return String.valueOf(value);
    }

    @Override
    public String visitStringLiteral(StringLiteralExpression expression) {
        String value = expression.value();
        boolean hasDoubleQuote = value.indexOf('"') != -1;
        boolean hasSingleQuote = value.indexOf('\'') != -1;
        char quote = (hasDoubleQuote && !hasSingleQuote) ? '\'' : '"';
        return quote + value + quote;
    }

    @Override
    public String visitBooleanLiteral(BooleanLiteralExpression expression) {
        return String.valueOf(expression.value());
    }

    @Override
    public String visitIdentifier(IdentifierExpression expression) {
        return expression.name();
    }

    @Override
    public String visitBinary(BinaryExpression expression) {
        OperatorDefinition operator = expression.operator();
        return operand(expression.left(), operator, false)
                + " "
                + operator.symbol()
                + " "
                + operand(expression.right(), operator, true);
    }

    private String operand(Expression operand, OperatorDefinition parent, boolean isRightSide) {
        String text = operand.accept(this);
        return needsParentheses(operand, parent, isRightSide) ? "(" + text + ")" : text;
    }

    private boolean needsParentheses(
            Expression operand, OperatorDefinition parent, boolean isRightSide) {
        if (!(operand instanceof BinaryExpression child)) {
            return false;
        }
        Integer parentLevel = precedenceLevels.get(parent);
        Integer childLevel = precedenceLevels.get(child.operator());
        if (parentLevel == null || childLevel == null) {
            return true; // operator this formatter wasn't told about: parenthesize to be safe
        }
        return isRightSide ? childLevel <= parentLevel : childLevel < parentLevel;
    }

    @Override
    public String visitReadInput(ReadInputExpression expression) {
        return "readInput(" + expression.message().accept(this) + ")";
    }

    @Override
    public String visitReadEnv(ReadEnvExpression expression) {
        return "readEnv(" + expression.variableName().accept(this) + ")";
    }
}
