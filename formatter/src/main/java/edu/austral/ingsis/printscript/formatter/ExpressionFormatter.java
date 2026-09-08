package edu.austral.ingsis.printscript.formatter;

import edu.austral.ingsis.printscript.common.ast.BinaryExpression;
import edu.austral.ingsis.printscript.common.ast.ExpressionVisitor;
import edu.austral.ingsis.printscript.common.ast.IdentifierExpression;
import edu.austral.ingsis.printscript.common.ast.NumberLiteralExpression;
import edu.austral.ingsis.printscript.common.ast.StringLiteralExpression;

/** Renders an expression to text. Operators always get one space on each side — that's fixed. */
final class ExpressionFormatter implements ExpressionVisitor<String> {

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
    public String visitIdentifier(IdentifierExpression expression) {
        return expression.name();
    }

    @Override
    public String visitBinary(BinaryExpression expression) {
        return expression.left().accept(this)
                + " "
                + expression.operator().symbol()
                + " "
                + expression.right().accept(this);
    }
}
