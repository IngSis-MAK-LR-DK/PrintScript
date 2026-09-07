package edu.austral.ingsis.printscript.common.ast;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Optional;

import edu.austral.ingsis.printscript.common.CoreOperators;
import edu.austral.ingsis.printscript.common.OperatorDefinition;
import edu.austral.ingsis.printscript.common.OperatorPrecedence;
import edu.austral.ingsis.printscript.common.Position;

import org.junit.jupiter.api.Test;

/**
 * Each node kind's visitor is its own single-method interface (Acyclic Visitor pattern - see the
 * javadoc on {@link StatementVisitor}/{@link ExpressionVisitor}), so a class that wants to handle
 * every kind implements all of the per-kind interfaces at once - an anonymous class can't do that
 * (it can only implement one interface), hence the named nested classes below.
 */
class VisitorDispatchTest {

    private static final Position P = new Position(1, 1);

    @Test
    void expressionVisitorDispatchesToTheRightMethod() {
        Expression number = new NumberLiteralExpression(1, P, P);
        Expression string = new StringLiteralExpression("a", P, P);
        Expression identifier = new IdentifierExpression("x", P, P);
        Expression binary = new BinaryExpression(number, stubOperator(), number, P, P);

        AllExpressionsVisitor visitor = new AllExpressionsVisitor();

        assertEquals("number", number.accept(visitor));
        assertEquals("string", string.accept(visitor));
        assertEquals("identifier", identifier.accept(visitor));
        assertEquals("binary", binary.accept(visitor));
    }

    @Test
    void aVisitorThatOnlyImplementsOneNodeKindWorksForThatKindAndFailsClearlyForOthers() {
        Expression number = new NumberLiteralExpression(1, P, P);
        Expression string = new StringLiteralExpression("a", P, P);

        NumberLiteralVisitor<String> onlyNumbers = expression -> "number";

        assertEquals("number", number.accept(onlyNumbers));
        assertThrows(UnsupportedOperationException.class, () -> string.accept(onlyNumbers));
    }

    private static OperatorDefinition stubOperator() {
        return new OperatorDefinition() {
            @Override
            public String symbol() {
                return "%";
            }

            @Override
            public OperatorPrecedence precedence() {
                return OperatorPrecedence.higherThan(CoreOperators.PLUS, CoreOperators.MINUS);
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

        AllStatementsVisitor visitor = new AllStatementsVisitor();

        assertEquals("declaration", declaration.accept(visitor));
        assertEquals("assignment", assignment.accept(visitor));
        assertEquals("println", println.accept(visitor));
    }

    @Test
    void aVisitorThatOnlyImplementsOneStatementKindWorksForThatKindAndFailsClearlyForOthers() {
        Expression literal = new NumberLiteralExpression(1, P, P);
        Statement println = new PrintlnStatement(literal, P, P);
        Statement assignment = new AssignmentStatement("x", literal, P, P);

        PrintlnVisitor<String> onlyPrintln = statement -> "println";

        assertEquals("println", println.accept(onlyPrintln));
        assertThrows(UnsupportedOperationException.class, () -> assignment.accept(onlyPrintln));
    }

    private static final class AllExpressionsVisitor
            implements NumberLiteralVisitor<String>,
                    StringLiteralVisitor<String>,
                    IdentifierVisitor<String>,
                    BinaryVisitor<String> {

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
    }

    private static final class AllStatementsVisitor
            implements VariableDeclarationVisitor<String>,
                    AssignmentVisitor<String>,
                    PrintlnVisitor<String> {

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
    }
}
