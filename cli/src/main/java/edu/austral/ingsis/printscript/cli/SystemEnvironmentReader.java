package edu.austral.ingsis.printscript.cli;

import java.util.Optional;

import edu.austral.ingsis.printscript.interpreter.EnvironmentReader;

/** Reads from the real OS environment ({@link System#getenv(String)}). */
final class SystemEnvironmentReader implements EnvironmentReader {

    @Override
    public Optional<String> read(String name) {
        return Optional.ofNullable(System.getenv(name));
    }
}
