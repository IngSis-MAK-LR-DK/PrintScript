package edu.austral.ingsis.printscript.cli;

import java.nio.file.Path;
import java.util.Optional;

import edu.austral.ingsis.printscript.common.Version;

record CliArguments(
        Operation operation, Path sourceFile, Version version, Optional<Path> configFile) {

    static final String USAGE =
            """
            Usage: printscript <validation|execution|formatting|analyzing> <file> [--version 1.0|1.1] [--config <path>]
            """;

    static CliArguments parse(String[] args) {
        if (args.length < 2) {
            throw new CliUsageException(USAGE);
        }

        Operation operation = Operation.fromArgument(args[0]);
        Path sourceFile = Path.of(args[1]);
        String versionLabel = Version.V1_0.label();
        Path configFile = null;

        for (int i = 2; i < args.length; i++) {
            String flag = args[i];
            switch (flag) {
                case "--version" -> versionLabel = valueFor(args, flag, i);
                case "--config" -> configFile = Path.of(valueFor(args, flag, i));
                default -> throw new CliUsageException("Unknown argument: " + flag + "\n" + USAGE);
            }
            i++;
        }

        Version version;
        try {
            version = Version.fromLabel(versionLabel);
        } catch (IllegalArgumentException e) {
            throw new CliUsageException(e.getMessage());
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
