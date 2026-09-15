package edu.austral.ingsis.printscript.formatter;

/**
 * What Jackson actually sees when it deserializes a YAML/JSON config: every field boxed, so a field
 * absent from the file comes through as {@code null} - distinguishable from an explicit {@code
 * false}/{@code 0}, which Java's primitive types can't do. {@link #resolve()} is the only place
 * that turns "absent" into the real default, by falling back to {@link
 * FormatterConfig#defaultConfig()} field by field.
 */
record RawFormatterConfig(
        Boolean spaceBeforeColon,
        Boolean spaceAfterColon,
        Boolean spaceAroundEquals,
        Integer newLinesBeforePrintln,
        Integer indentSize,
        Boolean spaceAroundParens) {

    FormatterConfig resolve() {
        FormatterConfig defaults = FormatterConfig.defaultConfig();
        return new FormatterConfig(
                spaceBeforeColon != null ? spaceBeforeColon : defaults.spaceBeforeColon(),
                spaceAfterColon != null ? spaceAfterColon : defaults.spaceAfterColon(),
                spaceAroundEquals != null ? spaceAroundEquals : defaults.spaceAroundEquals(),
                newLinesBeforePrintln != null
                        ? newLinesBeforePrintln
                        : defaults.newLinesBeforePrintln(),
                indentSize != null ? indentSize : defaults.indentSize(),
                spaceAroundParens != null ? spaceAroundParens : defaults.spaceAroundParens());
    }
}
