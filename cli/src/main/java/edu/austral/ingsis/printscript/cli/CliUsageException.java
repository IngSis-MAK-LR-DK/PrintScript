package edu.austral.ingsis.printscript.cli;

/** Raised for malformed CLI invocations (missing arguments, unknown flags, bad version). */
final class CliUsageException extends RuntimeException {

    CliUsageException(String message) {
        super(message);
    }
}
