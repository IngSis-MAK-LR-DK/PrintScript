package edu.austral.ingsis.printscript.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Path;

import edu.austral.ingsis.printscript.config.ConfigFormat;

import org.junit.jupiter.api.Test;

class ConfigFilesTest {

    @Test
    void recognizesYamlExtension() {
        assertEquals(ConfigFormat.YAML, ConfigFiles.formatOf(Path.of("rules.yaml")));
    }

    @Test
    void recognizesYmlExtension() {
        assertEquals(ConfigFormat.YAML, ConfigFiles.formatOf(Path.of("rules.yml")));
    }

    @Test
    void recognizesJsonExtension() {
        assertEquals(ConfigFormat.JSON, ConfigFiles.formatOf(Path.of("rules.json")));
    }

    @Test
    void recognizesExtensionCaseInsensitively() {
        assertEquals(ConfigFormat.YAML, ConfigFiles.formatOf(Path.of("RULES.YAML")));
    }

    @Test
    void throwsOnUnknownExtension() {
        assertThrows(CliUsageException.class, () -> ConfigFiles.formatOf(Path.of("rules.txt")));
    }
}
