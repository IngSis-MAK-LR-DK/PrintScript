package edu.austral.ingsis.printscript.cli;

/** Bad CLI usage — missing arguments, an unknown flag, an unsupported version. */
final class CliUsageException extends RuntimeException {

    CliUsageException(String message) {
        super(message);
    }
}
