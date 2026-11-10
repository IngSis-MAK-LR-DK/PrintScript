package edu.austral.ingsis.printscript.config;

/**
 * A config file was read fine but its content isn't valid: malformed YAML/JSON, an unknown option,
 * a value of the wrong type or out of range.
 */
public final class ConfigException extends RuntimeException {

    public ConfigException(String message) {
        super(message);
    }

    public ConfigException(String message, Throwable cause) {
        super(message, cause);
    }
}
