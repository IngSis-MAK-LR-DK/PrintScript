package edu.austral.ingsis.printscript.common;

import java.util.Set;

/**
 * Where an operator sits, expressed only relative to other already-known operators — never as a raw
 * number a plugin author would have to guess (and risk silently colliding with another plugin's
 * guess). {@code higherThan} operators bind less tightly than this one; {@code lowerThan} operators
 * bind more tightly than this one. Both may be empty — only a "root" operator (currently just the
 * core additive operators, see {@link CoreOperators}) has no constraints at all.
 *
 * <p>Two operators with no relation to each other, directly or transitively, end up at the same
 * binding level and are resolved by reading order (left-to-right) — exactly like {@code +} and
 * {@code -} already are today. See {@link OperatorPrecedenceResolver} for how these constraints
 * turn into concrete levels the parser can compare.
 */
public final class OperatorPrecedence {

    private final Set<OperatorDefinition> higherThan;
    private final Set<OperatorDefinition> lowerThan;

    private OperatorPrecedence(
            Set<OperatorDefinition> higherThan, Set<OperatorDefinition> lowerThan) {
        this.higherThan = Set.copyOf(higherThan);
        this.lowerThan = Set.copyOf(lowerThan);
    }

    /** No constraints at all. Only a "root" operator with nothing below it should use this. */
    public static OperatorPrecedence root() {
        return new OperatorPrecedence(Set.of(), Set.of());
    }

    /**
     * Binds more tightly than every operator listed (e.g. "same tier as {@code *}" is just {@code
     * higherThan(PLUS, MINUS)}, since nothing else pins it any higher).
     */
    public static OperatorPrecedence higherThan(OperatorDefinition... operators) {
        return new OperatorPrecedence(Set.of(operators), Set.of());
    }

    /**
     * Adds "binds less tightly than every operator listed" on top of the {@code higherThan} set
     * already declared — use this to pin an operator strictly between two others, e.g. {@code
     * higherThan(PLUS, MINUS).andLowerThan(MULTIPLY, DIVIDE)}.
     */
    public OperatorPrecedence andLowerThan(OperatorDefinition... operators) {
        return new OperatorPrecedence(higherThan, Set.of(operators));
    }

    Set<OperatorDefinition> higherThanOperators() {
        return higherThan;
    }

    Set<OperatorDefinition> lowerThanOperators() {
        return lowerThan;
    }
}
