package edu.austral.ingsis.printscript.cli;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

import edu.austral.ingsis.printscript.config.ConfigException;
import edu.austral.ingsis.printscript.config.ConfigFormat;
import edu.austral.ingsis.printscript.config.ConfigLoader;

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

    /**
     * Loads {@code configFile} with {@code loader}. Any problem with the config file - missing,
     * unreadable or invalid - surfaces as a {@link CliUsageException} that names the file, so it
     * never gets mistaken for a problem with the source file.
     */
    static <T> T load(Path configFile, ConfigLoader<T> loader) {
        ConfigFormat format = formatOf(configFile);
        try (Reader reader = Files.newBufferedReader(configFile, StandardCharsets.UTF_8)) {
            return loader.load(reader, format);
        } catch (NoSuchFileException e) {
            throw new CliUsageException("Config file not found: " + configFile);
        } catch (IOException e) {
            throw unreadable(configFile, e);
        } catch (UncheckedIOException e) {
            throw unreadable(configFile, e.getCause());
        } catch (ConfigException e) {
            throw new CliUsageException(
                    "Invalid config file '" + configFile + "': " + e.getMessage());
        }
    }

    private static CliUsageException unreadable(Path configFile, IOException cause) {
        return new CliUsageException(
                "Could not read config file '" + configFile + "': " + cause.getMessage());
    }
}
