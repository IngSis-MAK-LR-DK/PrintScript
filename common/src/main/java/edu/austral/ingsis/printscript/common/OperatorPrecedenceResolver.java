package edu.austral.ingsis.printscript.common;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Turns each installed operator's relative {@link OperatorPrecedence} constraints into a concrete
 * level the parser can compare with a plain > or <.
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
