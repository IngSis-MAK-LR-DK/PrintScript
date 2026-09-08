package edu.austral.ingsis.printscript.interpreter;

/**
 * Where a running program's {@code println} output goes. One line per call — how that line ends up
 * on screen, in a buffer, or handed off to something else entirely is up to whoever implements
 * this; the interpreter itself only knows it has lines to emit.
 */
@FunctionalInterface
public interface Emitter {

    void emit(String line);
}
