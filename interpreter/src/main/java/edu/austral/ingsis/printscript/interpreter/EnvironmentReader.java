package edu.austral.ingsis.printscript.interpreter;

import java.util.Optional;

/**
 * Answers {@code readEnv(name)} calls. The CLI backs this with real OS environment variables; tests
 * can stub it trivially with a lambda or a map lookup.
 */
@FunctionalInterface
public interface EnvironmentReader {

    Optional<String> read(String name);

    /** Same reasoning as {@link InputProvider#unsupported()}. */
    static EnvironmentReader unsupported() {
        return name -> {
            throw new UnsupportedOperationException(
                    "This ExecutionContext does not support readEnv(...)");
        };
    }
}
