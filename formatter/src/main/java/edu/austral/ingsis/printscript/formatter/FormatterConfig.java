package edu.austral.ingsis.printscript.formatter;

/**
 * Configurable formatting rules:
 *
 * @param spaceBeforeColon space before the {@code :} in a declaration
 * @param spaceAfterColon space after the {@code :} in a declaration
 * @param spaceAroundEquals space before and after the {@code =} in a declaration or assignment
 * @param newLinesBeforePrintln extra line breaks (0, 1 or 2) inserted right before a {@code
 *     println} call
 * @param indentSize how many spaces to indent the content of an {@code if}/{@code else} block
 *     relative to the line that opens it
 */
public record FormatterConfig(
        boolean spaceBeforeColon,
        boolean spaceAfterColon,
        boolean spaceAroundEquals,
        int newLinesBeforePrintln,
        int indentSize) {

    public FormatterConfig {
        if (newLinesBeforePrintln < 0 || newLinesBeforePrintln > 2) {
            throw new IllegalArgumentException("newLinesBeforePrintln must be 0, 1 or 2");
        }
        if (indentSize < 0) {
            throw new IllegalArgumentException("indentSize must not be negative");
        }
    }

    /**
     * Compatibility constructor for call sites that predate {@code if}/{@code else} blocks:
     * defaults to a 2-space indent.
     */
    public FormatterConfig(
            boolean spaceBeforeColon,
            boolean spaceAfterColon,
            boolean spaceAroundEquals,
            int newLinesBeforePrintln) {
        this(spaceBeforeColon, spaceAfterColon, spaceAroundEquals, newLinesBeforePrintln, 2);
    }

    public static FormatterConfig defaultConfig() {
        return new FormatterConfig(false, true, true, 0, 2);
    }
}
