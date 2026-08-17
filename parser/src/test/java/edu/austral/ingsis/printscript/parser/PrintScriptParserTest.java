package edu.austral.ingsis.printscript.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.austral.ingsis.printscript.common.OperatorDefinition;
import edu.austral.ingsis.printscript.common.SyntaxException;
import edu.austral.ingsis.printscript.common.Token;
import edu.austral.ingsis.printscript.common.ast.AssignmentStatement;
import edu.austral.ingsis.printscript.common.ast.BinaryExpression;
import edu.austral.ingsis.printscript.common.ast.BinaryOperator;
import edu.austral.ingsis.printscript.common.ast.ExtendedBinaryExpression;
import edu.austral.ingsis.printscript.common.ast.PrintlnStatement;
import edu.austral.ingsis.printscript.common.ast.Statement;
import edu.austral.ingsis.printscript.common.ast.StringLiteralExpression;
import edu.austral.ingsis.printscript.common.ast.VariableDeclarationStatement;
import edu.austral.ingsis.printscript.lexer.PrintScriptLexer;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class PrintScriptParserTest {

    private final PrintScriptLexer lexer = new PrintScriptLexer();
    private final PrintScriptParser parser = new PrintScriptParser();

    private List<Statement> parse(String source) {
        Iterator<Token> tokens = lexer.tokenize(new StringReader(source));
        List<Statement> statements = new ArrayList<>();
        parser.parse(tokens).forEachRemaining(statements::add);
        return statements;
    }

    @Test
    void parsesVariableDeclarationWithInitializer() {
        List<Statement> statements = parse("let x: number = 12;");

        assertEquals(1, statements.size());
        var declaration = assertInstanceOf(VariableDeclarationStatement.class, statements.get(0));
        assertEquals("x", declaration.identifierName());
        assertEquals("number", declaration.typeName());
        assertTrue(declaration.initializer().isPresent());
    }

    @Test
    void parsesVariableDeclarationWithoutInitializer() {
        List<Statement> statements = parse("let x: string;");

        var declaration = assertInstanceOf(VariableDeclarationStatement.class, statements.get(0));
        assertTrue(declaration.initializer().isEmpty());
    }

    @Test
    void parsesAssignment() {
        List<Statement> statements = parse("x = 5;");

        var assignment = assertInstanceOf(AssignmentStatement.class, statements.get(0));
        assertEquals("x", assignment.identifierName());
    }

    @Test
    void parsesPrintlnWithConcatenation() {
        List<Statement> statements = parse("println(\"Result: \" + a);");

        var println = assertInstanceOf(PrintlnStatement.class, statements.get(0));
        var concatenation = assertInstanceOf(BinaryExpression.class, println.argument());
        assertEquals(BinaryOperator.PLUS, concatenation.operator());
        assertInstanceOf(StringLiteralExpression.class, concatenation.left());
    }

    @Test
    void respectsOperatorPrecedence() {
        // 2 + 3 * 4 must parse as 2 + (3 * 4)
        List<Statement> statements = parse("x = 2 + 3 * 4;");

        var assignment = assertInstanceOf(AssignmentStatement.class, statements.get(0));
        var addition = assertInstanceOf(BinaryExpression.class, assignment.value());
        assertEquals(BinaryOperator.PLUS, addition.operator());
        assertInstanceOf(BinaryExpression.class, addition.right());
    }

    @Test
    void parsesMultipleStatementsLazily() {
        List<Statement> statements =
                parse("let a: number = 1;\nlet b: number = 2;\nprintln(a + b);");

        assertEquals(3, statements.size());
    }

    @Test
    void throwsOnMissingSemicolon() {
        assertThrows(SyntaxException.class, () -> parse("let x: number = 1"));
    }

    @Test
    void throwsOnMissingColon() {
        assertThrows(SyntaxException.class, () -> parse("let x number = 1;"));
    }

    @Test
    void parsesExtensionOperatorWhenRegistered() {
        OperatorDefinition modulo = stubModuloOperator();
        PrintScriptLexer pluggableLexer = new PrintScriptLexer(Set.of(modulo));
        PrintScriptParser pluggableParser = new PrintScriptParser(Set.of(modulo));

        Iterator<Token> tokens = pluggableLexer.tokenize(new StringReader("x = 7 % 3;"));
        List<Statement> statements = new ArrayList<>();
        pluggableParser.parse(tokens).forEachRemaining(statements::add);

        var assignment = assertInstanceOf(AssignmentStatement.class, statements.get(0));
        var extended = assertInstanceOf(ExtendedBinaryExpression.class, assignment.value());
        assertEquals(modulo, extended.operator());
    }

    private static OperatorDefinition stubModuloOperator() {
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
}
