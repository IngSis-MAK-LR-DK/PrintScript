package edu.austral.ingsis.printscript.interpreter;

import edu.austral.ingsis.printscript.common.ast.Statement;
import java.io.PrintStream;
import java.util.Iterator;

/** Executes a PrintScript program, statement by statement, writing {@code println} output. */
public interface Interpreter {

    void interpret(Iterator<Statement> statements, PrintStream output);
}
