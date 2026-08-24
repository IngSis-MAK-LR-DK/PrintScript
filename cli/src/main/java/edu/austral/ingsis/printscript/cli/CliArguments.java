package edu.austral.ingsis.printscript.cli;

import java.nio.file.Path;
import java.util.Optional;

record CliArguments(
        Operation operation, Path sourceFile, String version, Optional<Path> configFile) {

    private static final String SUPPORTED_VERSION = "1.0";

    static final String USAGE =
            """
            Usage: printscript <validation|execution|formatting|analyzing> <file> [--version 1.0] [--config <path>]
            """;

    static CliArguments parse(String[] args) {
        if (args.length < 2) {
            throw new CliUsageException(USAGE);
        }

        Operation operation = Operation.fromArgument(args[0]);
        Path sourceFile = Path.of(args[1]);
        String version = SUPPORTED_VERSION;
        Path configFile = null;

        for (int i = 2; i < args.length; i++) {
            String flag = args[i];
            String value = valueFor(args, flag, i);
            switch (flag) {
                case "--version" -> version = value;
                case "--config" -> configFile = Path.of(value);
                default -> throw new CliUsageException("Unknown argument: " + flag + "\n" + USAGE);
            }
            i++;
        }

        if (!version.equals(SUPPORTED_VERSION)) {
            throw new CliUsageException(
                    "Unsupported PrintScript version: " + version + " (only 1.0 is supported)");
        }

        return new CliArguments(operation, sourceFile, version, Optional.ofNullable(configFile));
    }

    private static String valueFor(String[] args, String flag, int index) {
        if (index + 1 >= args.length) {
            throw new CliUsageException("Missing value for argument: " + flag);
        }
        return args[index + 1];
    }
}
