package edu.austral.ingsis.printscript.formatter;

import edu.austral.ingsis.printscript.common.ast.BinaryExpression;
import edu.austral.ingsis.printscript.common.ast.BinaryVisitor;
import edu.austral.ingsis.printscript.common.ast.ExtendedBinaryExpression;
import edu.austral.ingsis.printscript.common.ast.ExtendedBinaryVisitor;
import edu.austral.ingsis.printscript.common.ast.IdentifierExpression;
import edu.austral.ingsis.printscript.common.ast.IdentifierVisitor;
import edu.austral.ingsis.printscript.common.ast.NumberLiteralExpression;
import edu.austral.ingsis.printscript.common.ast.NumberLiteralVisitor;
import edu.austral.ingsis.printscript.common.ast.StringLiteralExpression;
import edu.austral.ingsis.printscript.common.ast.StringLiteralVisitor;

/** Renders an expression to text. Operators always get a single space on each side (fixed rule). */
final class ExpressionFormatter
        implements NumberLiteralVisitor<String>,
                StringLiteralVisitor<String>,
                IdentifierVisitor<String>,
                BinaryVisitor<String>,
                ExtendedBinaryVisitor<String> {

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
        String operator =
                switch (expression.operator()) {
                    case PLUS -> "+";
                    case MINUS -> "-";
                    case MULTIPLY -> "*";
                    case DIVIDE -> "/";
                };
        return expression.left().accept(this)
                + " "
                + operator
                + " "
                + expression.right().accept(this);
    }

    @Override
    public String visitExtendedBinary(ExtendedBinaryExpression expression) {
        return expression.left().accept(this)
                + " "
                + expression.operator().symbol()
                + " "
                + expression.right().accept(this);
    }
}
