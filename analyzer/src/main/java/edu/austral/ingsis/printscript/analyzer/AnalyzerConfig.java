package edu.austral.ingsis.printscript.analyzer;

import java.util.ArrayList;
import java.util.List;

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

    /** Turns this config into the list of rules that are actually active. */
    List<AnalysisRule> toRules() {
        List<AnalysisRule> rules = new ArrayList<>();
        if (identifierCaseCheckEnabled) {
            rules.add(new IdentifierCaseRule(identifierCase));
        }
        if (printlnArgumentMustBeIdentifierOrLiteral) {
            rules.add(new PrintlnArgumentRule());
        }
        return List.copyOf(rules);
    }
}
