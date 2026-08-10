package edu.austral.ingsis.printscript.common;

import java.io.Reader;

/** Reads a rule-configuration object (formatter or analyzer rules) out of a YAML/JSON source. */
public interface ConfigLoader<T> {

    T load(Reader source, ConfigFormat format);
}
