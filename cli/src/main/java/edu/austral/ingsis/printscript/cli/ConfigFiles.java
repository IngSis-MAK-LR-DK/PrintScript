package edu.austral.ingsis.printscript.cli;

import edu.austral.ingsis.printscript.common.ConfigFormat;
import java.nio.file.Path;

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
