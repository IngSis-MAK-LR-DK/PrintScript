package edu.austral.ingsis.printscript.analyzer;

/**
 * Static analysis rules configurable per the consigna:
 *
 * @param identifierCaseCheckEnabled whether identifier naming is checked at all
 * @param identifierCase which naming convention identifiers must follow when the check is on
 * @param printlnArgumentMustBeIdentifierOrLiteral whether {@code println} is restricted to taking
 *     only an identifier or a literal (no arbitrary expressions)
 */
public record AnalyzerConfig(
        boolean identifierCaseCheckEnabled,
        IdentifierCase identifierCase,
        boolean printlnArgumentMustBeIdentifierOrLiteral) {

    public static AnalyzerConfig defaultConfig() {
        return new AnalyzerConfig(true, IdentifierCase.CAMEL_CASE, true);
    }
}
