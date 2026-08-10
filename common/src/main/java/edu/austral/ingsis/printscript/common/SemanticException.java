package edu.austral.ingsis.printscript.common;

/**
 * Raised for errors that are only detectable once meaning is attached to the AST: type
 * mismatches, undeclared variables, etc. Used by both the interpreter and the static analyzer.
 */
public class SemanticException extends PrintScriptException {

    public SemanticException(String message, Position start, Position end) {
        super(message, start, end);
    }
}
