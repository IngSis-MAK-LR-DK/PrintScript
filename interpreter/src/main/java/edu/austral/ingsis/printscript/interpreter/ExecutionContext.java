package edu.austral.ingsis.printscript.interpreter;

/**
 * Everything an {@link Interpreter} needs from its caller to run a program: where {@code println}
 * output goes, and how to answer {@code readInput}/{@code readEnv}. Bundled into one object instead
 * of three separate parameters - a future need doesn't grow {@link Interpreter#interpret}'s
 * signature again, it just adds a field here.
 */
public record ExecutionContext(
        Emitter emitter, InputProvider inputProvider, EnvironmentReader environmentReader) {

    /**
     * Convenience for callers that don't use {@code readInput}/{@code readEnv} at all - a program
     * that never calls either never notices, and one that does gets a clear error instead of
     * silently blocking or producing a placeholder value.
     */
    public static ExecutionContext withEmitter(Emitter emitter) {
        return new ExecutionContext(
                emitter, InputProvider.unsupported(), EnvironmentReader.unsupported());
    }
}
