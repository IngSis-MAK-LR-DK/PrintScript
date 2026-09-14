package edu.austral.ingsis.printscript.analyzer;

/**
 * What Jackson actually sees when it deserializes a YAML/JSON config: every field boxed, so a field
 * absent from the file comes through as {@code null} - distinguishable from an explicit {@code
 * false}/{@code 0}, which Java's primitive types can't do. {@link #resolve()} is the only place
 * that turns "absent" into the real default, by falling back to {@link
 * AnalyzerConfig#defaultConfig()} field by field.
 */
record RawAnalyzerConfig(
        Boolean identifierCaseCheckEnabled,
        IdentifierCase identifierCase,
        Boolean printlnArgumentMustBeIdentifierOrLiteral,
        Boolean readArgumentMustBeIdentifierOrLiteral) {

    AnalyzerConfig resolve() {
        AnalyzerConfig defaults = AnalyzerConfig.defaultConfig();
        return new AnalyzerConfig(
                identifierCaseCheckEnabled != null
                        ? identifierCaseCheckEnabled
                        : defaults.identifierCaseCheckEnabled(),
                identifierCase != null ? identifierCase : defaults.identifierCase(),
                printlnArgumentMustBeIdentifierOrLiteral != null
                        ? printlnArgumentMustBeIdentifierOrLiteral
                        : defaults.printlnArgumentMustBeIdentifierOrLiteral(),
                readArgumentMustBeIdentifierOrLiteral != null
                        ? readArgumentMustBeIdentifierOrLiteral
                        : defaults.readArgumentMustBeIdentifierOrLiteral());
    }
}
