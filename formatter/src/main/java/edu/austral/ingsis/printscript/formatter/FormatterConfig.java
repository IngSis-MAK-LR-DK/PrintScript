package edu.austral.ingsis.printscript.formatter;

/**
 * Configurable formatting rules:
 *
 * @param spaceBeforeColon space before the {@code :} in a declaration
 * @param spaceAfterColon space after the {@code :} in a declaration
 * @param spaceAroundEquals space before and after the {@code =} in a declaration or assignment
 * @param newLinesBeforePrintln extra line breaks (0, 1 or 2) inserted between two
 *     <em>consecutive</em> {@code println} calls at the top level of the program - a {@code
 *     println} that follows a non-{@code println} statement, or the first statement of the file,
 *     never gets one
 * @param indentSize how many spaces to indent the content of an {@code if}/{@code else} block
 *     relative to the line that opens it
 * @param spaceAroundParens space right inside the parentheses of a {@code println} call, e.g.
 *     {@code println( x )} instead of {@code println(x)}
 */
public record FormatterConfig(
        boolean spaceBeforeColon,
        boolean spaceAfterColon,
        boolean spaceAroundEquals,
        int newLinesBeforePrintln,
        int indentSize,
        boolean spaceAroundParens) {

    public FormatterConfig {
        if (newLinesBeforePrintln < 0 || newLinesBeforePrintln > 2) {
            throw new IllegalArgumentException("newLinesBeforePrintln must be 0, 1 or 2");
        }
        if (indentSize < 0) {
            throw new IllegalArgumentException("indentSize must not be negative");
        }
    }

    /**
     * Compatibility constructor for call sites that predate {@code spaceAroundParens}: defaults to
     * no extra spacing inside {@code println}'s parentheses.
     */
    public FormatterConfig(
            boolean spaceBeforeColon,
            boolean spaceAfterColon,
            boolean spaceAroundEquals,
            int newLinesBeforePrintln,
            int indentSize) {
        this(
                spaceBeforeColon,
                spaceAfterColon,
                spaceAroundEquals,
                newLinesBeforePrintln,
                indentSize,
                false);
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
        return new FormatterConfig(false, true, true, 0, 2, false);
    }
}
