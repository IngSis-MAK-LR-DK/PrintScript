package edu.austral.ingsis.printscript.interpreter;

import java.util.Iterator;

import edu.austral.ingsis.printscript.common.ast.Statement;

/** Runs a PrintScript program statement by statement. */
public interface Interpreter {

    void interpret(Iterator<Statement> statements, ExecutionContext context);
}
