package edu.austral.ingsis.printscript.cli;

import java.nio.file.Path;

import edu.austral.ingsis.printscript.config.ConfigFormat;

final class ConfigFiles {

    private ConfigFiles() {}

    static ConfigFormat formatOf(Path configFile) {
        String name = configFile.getFileName().toString().toLowerCase(java.util.Locale.ROOT);
        if (name.endsWith(".yaml") || name.endsWith(".yml")) {
            return ConfigFormat.YAML;
        }
        if (name.endsWith(".json")) {
            return ConfigFormat.JSON;
        }
        throw new CliUsageException("Cannot determine config format from file name: " + configFile);
    }
}
