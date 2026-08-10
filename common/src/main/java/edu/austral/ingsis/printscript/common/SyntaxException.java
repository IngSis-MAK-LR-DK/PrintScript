package edu.austral.ingsis.printscript.common;

/** Raised by the parser when a token stream does not match the PrintScript grammar. */
public class SyntaxException extends PrintScriptException {

    public SyntaxException(String message, Position start, Position end) {
        super(message, start, end);
    }
}
