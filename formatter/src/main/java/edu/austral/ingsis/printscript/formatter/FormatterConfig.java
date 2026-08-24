package edu.austral.ingsis.printscript.formatter;

/**
 * Formatting rules configurable per the consigna:
 *
 * @param spaceBeforeColon space before the {@code :} in a declaration
 * @param spaceAfterColon space after the {@code :} in a declaration
 * @param spaceAroundEquals space before and after the {@code =} in a declaration/assignment
 * @param newLinesBeforePrintln extra line breaks (0, 1 or 2) inserted right before a {@code
 *     println} call
 */
public record FormatterConfig(
        boolean spaceBeforeColon,
        boolean spaceAfterColon,
        boolean spaceAroundEquals,
        int newLinesBeforePrintln) {

    public FormatterConfig {
        if (newLinesBeforePrintln < 0 || newLinesBeforePrintln > 2) {
            throw new IllegalArgumentException("newLinesBeforePrintln must be 0, 1 or 2");
        }
    }

    public static FormatterConfig defaultConfig() {
        return new FormatterConfig(false, true, true, 0);
    }
}
