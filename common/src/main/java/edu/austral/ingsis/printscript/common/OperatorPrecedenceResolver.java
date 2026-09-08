package edu.austral.ingsis.printscript.common;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Turns each installed operator's relative {@link OperatorPrecedence} constraints into a concrete
 * binding level the parser can compare with a plain {@code >}/{@code <}.
 *
 * <p>Levels are computed fresh from the complete set of installed operators every time (the four
 * core ones plus whatever plugins {@code ServiceLoader} found) — never patched into a pre-existing
 * fixed numbering. That's what lets a new operator get inserted strictly between two existing ones
 * without ever running out of room: unlike a scheme of fixed integers (e.g. {@code +}/{@code -} =
 * 1, {@code *}/{@code /} = 2), there's no gap to run out of, because the whole table is rebuilt
 * from the full constraint graph at once. An operator's {@code lowerThan} constraint contributes an
 * edge to the operator it targets even though that operator never declared anything about it —
 * that's how a plugin can push itself between two operators without either of them knowing it
 * exists (see {@code CoreOperators#MULTIPLY} being forced above a plugin that only declared {@code
 * lowerThan (MULTIPLY)}).
 *
 * <p>Two operators with no relation to each other, directly or transitively, land on the same level
 * — resolved by the parser reading left to right, exactly like {@code +} and {@code -} already are.
 * That's a deliberate, correct outcome, not a limitation: there's no information in the system to
 * justify ordering two mutually unaware operators one way or the other.
 */
public final class OperatorPrecedenceResolver {

    private OperatorPrecedenceResolver() {}

    public static Map<OperatorDefinition, Integer> resolveLevels(
            Set<OperatorDefinition> operators) {
        Map<OperatorDefinition, Set<OperatorDefinition>> mustBeHigherThan = new HashMap<>();
        for (OperatorDefinition operator : operators) {
            mustBeHigherThan.computeIfAbsent(operator, unused -> new HashSet<>());
        }
        for (OperatorDefinition operator : operators) {
            for (OperatorDefinition below : operator.precedence().higherThanOperators()) {
                mustBeHigherThan.computeIfAbsent(operator, unused -> new HashSet<>()).add(below);
            }
            for (OperatorDefinition above : operator.precedence().lowerThanOperators()) {
                mustBeHigherThan.computeIfAbsent(above, unused -> new HashSet<>()).add(operator);
            }
        }

        Map<OperatorDefinition, Integer> levels = new HashMap<>();
        Set<OperatorDefinition> inProgress = new HashSet<>();
        for (OperatorDefinition operator : operators) {
            levelOf(operator, mustBeHigherThan, levels, inProgress);
        }
        return Map.copyOf(levels);
    }

    private static int levelOf(
            OperatorDefinition operator,
            Map<OperatorDefinition, Set<OperatorDefinition>> mustBeHigherThan,
            Map<OperatorDefinition, Integer> levels,
            Set<OperatorDefinition> inProgress) {
        Integer known = levels.get(operator);
        if (known != null) {
            return known;
        }
        if (!inProgress.add(operator)) {
            throw new IllegalStateException(
                    "Contradictory operator precedence: a cycle involving '"
                            + operator.symbol()
                            + "' (it ends up needing to bind both tighter and looser than itself)");
        }
        int level = 0;
        for (OperatorDefinition below : mustBeHigherThan.getOrDefault(operator, Set.of())) {
            level = Math.max(level, 1 + levelOf(below, mustBeHigherThan, levels, inProgress));
        }
        inProgress.remove(operator);
        levels.put(operator, level);
        return level;
    }
}
