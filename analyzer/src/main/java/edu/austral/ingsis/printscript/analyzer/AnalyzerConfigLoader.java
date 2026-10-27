package edu.austral.ingsis.printscript.analyzer;

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

/** Reads an {@link AnalyzerConfig} out of a YAML or JSON file. */
public final class AnalyzerConfigLoader implements ConfigLoader<AnalyzerConfig> {

    private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
    private final ObjectMapper jsonMapper = new ObjectMapper();

    @Override
    public AnalyzerConfig load(Reader source, ConfigFormat format) {
        ObjectMapper mapper = format == ConfigFormat.YAML ? yamlMapper : jsonMapper;
        try {
            return mapper.readValue(source, RawAnalyzerConfig.class).resolve();
        } catch (UnrecognizedPropertyException e) {
            throw new ConfigException("Unknown option '" + e.getPropertyName() + "'", e);
        } catch (JsonProcessingException e) {
            throw new ConfigException(e.getOriginalMessage() + locationOf(e), e);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
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
