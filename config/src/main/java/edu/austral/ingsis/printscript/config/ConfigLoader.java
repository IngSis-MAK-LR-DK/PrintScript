package edu.austral.ingsis.printscript.config;

import java.io.Reader;

/** Reads a rule-configuration object (formatter or analyzer rules) out of a YAML/JSON source. */
public interface ConfigLoader<T> {

    /**
     * @throws ConfigException if the source's content isn't a valid config
     * @throws java.io.UncheckedIOException if the source itself can't be read
     */
    T load(Reader source, ConfigFormat format);
}
