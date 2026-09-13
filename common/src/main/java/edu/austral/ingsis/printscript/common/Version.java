package edu.austral.ingsis.printscript.common;

/**
 * Which PrintScript language version a {@code Lexer}/{@code Parser} should accept. The lexer always
 * recognizes every keyword across every version — it's the parser that decides, per {@code
 * Version}, which of those a program is actually allowed to use, so that a construct from a later
 * version surfaces as a clear, version-aware error instead of a generic syntax error.
 */
public enum Version {
    V1_0("1.0"),
    V1_1("1.1");

    private final String label;

    Version(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    /** True if this version is {@code other} or a later one — versions only ever add features. */
    public boolean isAtLeast(Version other) {
        return this.ordinal() >= other.ordinal();
    }

    public static Version fromLabel(String label) {
        for (Version version : values()) {
            if (version.label.equals(label)) {
                return version;
            }
        }
        throw new IllegalArgumentException("Unsupported PrintScript version: " + label);
    }
}
