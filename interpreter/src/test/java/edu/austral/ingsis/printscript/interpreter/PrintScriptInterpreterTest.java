package edu.austral.ingsis.printscript.interpreter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import edu.austral.ingsis.printscript.common.SemanticException;
import edu.austral.ingsis.printscript.common.ast.Statement;
import edu.austral.ingsis.printscript.lexer.PrintScriptLexer;
import edu.austral.ingsis.printscript.parser.PrintScriptParser;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import org.junit.jupiter.api.Test;

class PrintScriptInterpreterTest {

    private final PrintScriptLexer lexer = new PrintScriptLexer();
    private final PrintScriptParser parser = new PrintScriptParser();
    private final PrintScriptInterpreter interpreter = new PrintScriptInterpreter();

    private String run(String source) {
        Iterator<Statement> statements = parser.parse(lexer.tokenize(new StringReader(source)));
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        interpreter.interpret(statements, new PrintStream(buffer, true, StandardCharsets.UTF_8));
        return buffer.toString(StandardCharsets.UTF_8).replace("\r\n", "\n");
    }

    @Test
    void example1_stringConcatenation() {
        String output =
                run(
                        """
                        let name: string = "Joe";
                        let lastName: string = "Doe";
                        println(name + " " + lastName);
                        """);

        assertEquals("Joe Doe\n", output);
    }

    @Test
    void example2_numberDivisionInStringConcatenation() {
        String output =
                run(
                        """
                        let a: number = 12;
                        let b: number = 4;
                        let c: number = a / b;
                        println("Result: " + c);
                        """);

        assertEquals("Result: 3\n", output);
    }

    @Test
    void example3_reassignmentOfADeclaredVariable() {
        String output =
                run(
                        """
                        let a: number = 12;
                        let b: number = 4;
                        a = a / b;
                        println("Result: " + a);
                        """);

        assertEquals("Result: 3\n", output);
    }

    @Test
    void arithmeticOperationsOnNumbers() {
        String output = run("println(2 + 3 * 4);");

        assertEquals("14\n", output);
    }

    @Test
    void throwsWhenUsingAnUndeclaredVariable() {
        assertThrows(SemanticException.class, () -> run("println(x);"));
    }

    @Test
    void throwsWhenAssigningWrongType() {
        assertThrows(SemanticException.class, () -> run("let x: number = \"hello\";"));
    }

    @Test
    void throwsWhenUsingVariableBeforeAssignment() {
        assertThrows(SemanticException.class, () -> run("let x: number;\nprintln(x);"));
    }
}
