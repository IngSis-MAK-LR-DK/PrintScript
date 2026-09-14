package edu.austral.ingsis.printscript.interpreter;

/**
 * Answers {@code readInput(message)} calls. The CLI backs this with real standard input; tests (and
 * a validation pass that never wants to block on real input) can stub it trivially with a lambda.
 */
@FunctionalInterface
public interface InputProvider {

    String read(String prompt);

    /**
     * Every call fails clearly instead of silently returning a placeholder or blocking on real
     * input - the right default for a context (like semantic validation) that never intends to
     * actually run {@code readInput}.
     */
    static InputProvider unsupported() {
        return prompt -> {
            throw new UnsupportedOperationException(
                    "This ExecutionContext does not support readInput(...)");
        };
    }
}
