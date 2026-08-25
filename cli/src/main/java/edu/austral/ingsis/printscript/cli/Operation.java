package edu.austral.ingsis.printscript.cli;

import java.util.Locale;

enum Operation {
    VALIDATION,
    EXECUTION,
    FORMATTING,
    ANALYZING;

    static Operation fromArgument(String argument) {
        try {
            return Operation.valueOf(argument.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new CliUsageException(
                    "Unknown operation '"
                            + argument
                            + "'. Expected one of: validation, execution, formatting, analyzing");
        }
    }
}
