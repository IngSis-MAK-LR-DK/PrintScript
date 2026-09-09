package edu.austral.ingsis.printscript.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

class OperatorPrecedenceResolverTest {

    @Test
    void aRootOperatorGetsLevelZero() {
        Map<OperatorDefinition, Integer> levels =
                OperatorPrecedenceResolver.resolveLevels(Set.of(CoreOperators.PLUS));

        assertEquals(0, levels.get(CoreOperators.PLUS));
    }

    @Test
    void anOperatorHigherThanAnotherGetsExactlyOneLevelAbove() {
        Map<OperatorDefinition, Integer> levels =
                OperatorPrecedenceResolver.resolveLevels(CoreOperators.all());

        assertEquals(0, levels.get(CoreOperators.PLUS));
        assertEquals(1, levels.get(CoreOperators.MULTIPLY));
    }

    @Test
    void aPluginBetweenTwoOperatorsPushesTheUpperOneUpEvenThoughItNeverHeardOfThePlugin() {
        // A plugin that declares "higherThan(+, -), lowerThan(*, /)" - i.e. strictly between the
        // two
        // core tiers. CoreOperators.MULTIPLY/DIVIDE only ever declared "higherThan(+, -)"; they
        // know
        // nothing about this plugin, yet still end up correctly pushed above it.
        OperatorDefinition between =
                stubOperator(
                        "@",
                        OperatorPrecedence.higherThan(CoreOperators.PLUS, CoreOperators.MINUS)
                                .andLowerThan(CoreOperators.MULTIPLY, CoreOperators.DIVIDE));

        Set<OperatorDefinition> installed = new HashSet<>(CoreOperators.all());
        installed.add(between);
        Map<OperatorDefinition, Integer> levels =
                OperatorPrecedenceResolver.resolveLevels(installed);

        int plus = levels.get(CoreOperators.PLUS);
        int betweenLevel = levels.get(between);
        int multiply = levels.get(CoreOperators.MULTIPLY);

        assertEquals(plus + 1, betweenLevel, "should sit exactly one level above +/-");
        assertEquals(betweenLevel + 1, multiply, "* should get pushed strictly above the plugin");
    }

    @Test
    void twoMutuallyUnawarePluginsBetweenTheSameTwoTiersEndUpAtTheSameLevel() {
        // Neither plugin declares anything about the other - there's no information to justify
        // ordering one before the other, so they land on the same level (resolved by the parser
        // reading left to right, same as + and - already are).
        OperatorPrecedence sameConstraint =
                OperatorPrecedence.higherThan(CoreOperators.PLUS, CoreOperators.MINUS)
                        .andLowerThan(CoreOperators.MULTIPLY, CoreOperators.DIVIDE);
        OperatorDefinition first = stubOperator("@", sameConstraint);
        OperatorDefinition second = stubOperator("#", sameConstraint);

        Set<OperatorDefinition> installed = new HashSet<>(CoreOperators.all());
        installed.add(first);
        installed.add(second);
        Map<OperatorDefinition, Integer> levels =
                OperatorPrecedenceResolver.resolveLevels(installed);

        assertEquals(levels.get(first), levels.get(second));
    }

    @Test
    void anOperatorWithNoRelationToAnythingSharesTheRootLevel() {
        OperatorDefinition standalone = stubOperator("@", OperatorPrecedence.root());

        Map<OperatorDefinition, Integer> levels =
                OperatorPrecedenceResolver.resolveLevels(Set.of(CoreOperators.PLUS, standalone));

        assertEquals(levels.get(CoreOperators.PLUS), levels.get(standalone));
    }

    @Test
    void throwsOnAContradictoryCycle() {
        // first must be higher than second, but second also declares itself higher than first -
        // there's no consistent level assignment that satisfies both.
        StubHolder holder = new StubHolder();
        holder.first = stubOperator("@", () -> OperatorPrecedence.higherThan(holder.second));
        holder.second = stubOperator("#", () -> OperatorPrecedence.higherThan(holder.first));

        assertThrows(
                IllegalStateException.class,
                () ->
                        OperatorPrecedenceResolver.resolveLevels(
                                Set.of(holder.first, holder.second)));
    }

    private static final class StubHolder {
        OperatorDefinition first;
        OperatorDefinition second;
    }

    private static OperatorDefinition stubOperator(String symbol, OperatorPrecedence precedence) {
        return stubOperator(symbol, () -> precedence);
    }

    private static OperatorDefinition stubOperator(
            String symbol, Supplier<OperatorPrecedence> precedence) {
        return new OperatorDefinition() {
            @Override
            public String symbol() {
                return symbol;
            }

            @Override
            public OperatorPrecedence precedence() {
                return precedence.get();
            }

            @Override
            public double apply(double left, double right) {
                return 0;
            }
        };
    }
}
