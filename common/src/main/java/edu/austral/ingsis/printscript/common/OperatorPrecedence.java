package edu.austral.ingsis.printscript.common;

import java.util.Set;

/** Where an operator sits, expressed only relative to other already-known operators. */
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
     * higherThan(PLUS, MINUS)}).
     */
    public static OperatorPrecedence higherThan(OperatorDefinition... operators) {
        return new OperatorPrecedence(Set.of(operators), Set.of());
    }

    /** e.g. {@code higherThan(PLUS, MINUS).andLowerThan(MULTIPLY, DIVIDE)}. */
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
