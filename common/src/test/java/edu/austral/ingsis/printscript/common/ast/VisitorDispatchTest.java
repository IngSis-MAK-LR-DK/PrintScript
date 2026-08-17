package edu.austral.ingsis.printscript.common.ast;

import static org.junit.jupiter.api.Assertions.assertEquals;

import edu.austral.ingsis.printscript.common.OperatorDefinition;
import edu.austral.ingsis.printscript.common.Position;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class VisitorDispatchTest {

    private static final Position P = new Position(1, 1);

    @Test
    void expressionVisitorDispatchesToTheRightMethod() {
        Expression number = new NumberLiteralExpression(1, P, P);
        Expression string = new StringLiteralExpression("a", P, P);
        Expression identifier = new IdentifierExpression("x", P, P);
        Expression binary = new BinaryExpression(number, BinaryOperator.PLUS, number, P, P);
        Expression extendedBinary = new ExtendedBinaryExpression(number, stubOperator(), number, P, P);

        ExpressionVisitor<String> visitor =
                new ExpressionVisitor<>() {
                    @Override
                    public String visitNumberLiteral(NumberLiteralExpression expression) {
                        return "number";
                    }

                    @Override
                    public String visitStringLiteral(StringLiteralExpression expression) {
                        return "string";
                    }

                    @Override
                    public String visitIdentifier(IdentifierExpression expression) {
                        return "identifier";
                    }

                    @Override
                    public String visitBinary(BinaryExpression expression) {
                        return "binary";
                    }

                    @Override
                    public String visitExtendedBinary(ExtendedBinaryExpression expression) {
                        return "extendedBinary";
                    }
                };

        assertEquals("number", number.accept(visitor));
        assertEquals("string", string.accept(visitor));
        assertEquals("identifier", identifier.accept(visitor));
        assertEquals("binary", binary.accept(visitor));
        assertEquals("extendedBinary", extendedBinary.accept(visitor));
    }

    private static OperatorDefinition stubOperator() {
        return new OperatorDefinition() {
            @Override
            public String symbol() {
                return "%";
            }

            @Override
            public double apply(double left, double right) {
                return left % right;
            }
        };
    }

    @Test
    void statementVisitorDispatchesToTheRightMethod() {
        Expression literal = new NumberLiteralExpression(1, P, P);
        Statement declaration =
                new VariableDeclarationStatement("x", "number", Optional.of(literal), P, P);
        Statement assignment = new AssignmentStatement("x", literal, P, P);
        Statement println = new PrintlnStatement(literal, P, P);

        StatementVisitor<String> visitor =
                new StatementVisitor<>() {
                    @Override
                    public String visitVariableDeclaration(VariableDeclarationStatement statement) {
                        return "declaration";
                    }

                    @Override
                    public String visitAssignment(AssignmentStatement statement) {
                        return "assignment";
                    }

                    @Override
                    public String visitPrintln(PrintlnStatement statement) {
                        return "println";
                    }
                };

        assertEquals("declaration", declaration.accept(visitor));
        assertEquals("assignment", assignment.accept(visitor));
        assertEquals("println", println.accept(visitor));
    }
}
