package edu.austral.ingsis.printscript.formatter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import edu.austral.ingsis.printscript.config.ConfigFormat;
import edu.austral.ingsis.printscript.config.ConfigLoader;
import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;

/** Reads a {@link FormatterConfig} out of a YAML or JSON file. */
public final class FormatterConfigLoader implements ConfigLoader<FormatterConfig> {

    private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
    private final ObjectMapper jsonMapper = new ObjectMapper();

    @Override
    public FormatterConfig load(Reader source, ConfigFormat format) {
        ObjectMapper mapper = format == ConfigFormat.YAML ? yamlMapper : jsonMapper;
        try {
            return mapper.readValue(source, FormatterConfig.class);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
