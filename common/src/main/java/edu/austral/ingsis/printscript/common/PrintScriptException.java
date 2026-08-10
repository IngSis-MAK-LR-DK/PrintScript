package edu.austral.ingsis.printscript.common;

/** Base type for every error PrintScript tools report, always tied to a source range. */
public abstract class PrintScriptException extends RuntimeException {

    private final Position start;
    private final Position end;

    protected PrintScriptException(String message, Position start, Position end) {
        super(message);
        this.start = start;
        this.end = end;
    }

    public Position start() {
        return start;
    }

    public Position end() {
        return end;
    }
}
