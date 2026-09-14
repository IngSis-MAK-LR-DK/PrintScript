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
 * @param readArgumentMustBeIdentifierOrLiteral whether {@code readInput}/{@code readEnv} are
 *     restricted to taking only an identifier or a literal as their argument (no arbitrary
 *     expressions)
 */
public record AnalyzerConfig(
        boolean identifierCaseCheckEnabled,
        IdentifierCase identifierCase,
        boolean printlnArgumentMustBeIdentifierOrLiteral,
        boolean readArgumentMustBeIdentifierOrLiteral) {

    /**
     * Compatibility constructor for callers built before {@link
     * #readArgumentMustBeIdentifierOrLiteral} existed. Note this default only applies to hand-built
     * instances - Jackson deserializes YAML/JSON straight through the canonical (4-arg)
     * constructor, so a config file that omits this field gets Java's raw {@code false} default
     * instead, the same gotcha already documented on {@code FormatterConfig}.
     */
    public AnalyzerConfig(
            boolean identifierCaseCheckEnabled,
            IdentifierCase identifierCase,
            boolean printlnArgumentMustBeIdentifierOrLiteral) {
        this(
                identifierCaseCheckEnabled,
                identifierCase,
                printlnArgumentMustBeIdentifierOrLiteral,
                true);
    }

    public static AnalyzerConfig defaultConfig() {
        return new AnalyzerConfig(true, IdentifierCase.CAMEL_CASE, true, true);
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
        if (readArgumentMustBeIdentifierOrLiteral) {
            rules.add(new ReadArgumentRule());
        }
        return List.copyOf(rules);
    }
}
