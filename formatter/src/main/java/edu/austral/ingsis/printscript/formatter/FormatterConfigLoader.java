package edu.austral.ingsis.printscript.formatter;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;

import edu.austral.ingsis.printscript.config.ConfigException;
import edu.austral.ingsis.printscript.config.ConfigFormat;
import edu.austral.ingsis.printscript.config.ConfigLoader;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

/** Reads a {@link FormatterConfig} out of a YAML or JSON file. */
public final class FormatterConfigLoader implements ConfigLoader<FormatterConfig> {

    private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
    private final ObjectMapper jsonMapper = new ObjectMapper();

    @Override
    public FormatterConfig load(Reader source, ConfigFormat format) {
        ObjectMapper mapper = format == ConfigFormat.YAML ? yamlMapper : jsonMapper;
        RawFormatterConfig raw;
        try {
            raw = mapper.readValue(source, RawFormatterConfig.class);
        } catch (UnrecognizedPropertyException e) {
            throw new ConfigException("Unknown option '" + e.getPropertyName() + "'", e);
        } catch (JsonProcessingException e) {
            throw new ConfigException(e.getOriginalMessage() + locationOf(e), e);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        try {
            return raw.resolve();
        } catch (IllegalArgumentException e) {
            throw new ConfigException(e.getMessage(), e);
        }
    }

    private static String locationOf(JsonProcessingException e) {
        JsonLocation location = e.getLocation();
        if (location == null) {
            return "";
        }
        return " (line " + location.getLineNr() + ", column " + location.getColumnNr() + ")";
    }
}
