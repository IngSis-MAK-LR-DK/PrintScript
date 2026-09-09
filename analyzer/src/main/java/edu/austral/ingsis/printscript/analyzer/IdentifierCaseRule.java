package edu.austral.ingsis.printscript.analyzer;

import java.util.Optional;
import java.util.regex.Pattern;

import edu.austral.ingsis.printscript.common.ast.Statement;
import edu.austral.ingsis.printscript.common.ast.VariableDeclarationStatement;

/** Checks that a declared identifier follows the configured naming convention. */
final class IdentifierCaseRule implements AnalysisRule {

    private static final Pattern CAMEL_CASE = Pattern.compile("^[a-z][a-zA-Z0-9]*$");
    private static final Pattern SNAKE_CASE = Pattern.compile("^[a-z][a-z0-9_]*$");

    private final IdentifierCase requiredCase;

    IdentifierCaseRule(IdentifierCase requiredCase) {
        this.requiredCase = requiredCase;
    }

    @Override
    public Optional<AnalysisFinding> check(Statement statement) {
        if (!(statement instanceof VariableDeclarationStatement declaration)) {
            return Optional.empty();
        }
        Pattern pattern = requiredCase == IdentifierCase.CAMEL_CASE ? CAMEL_CASE : SNAKE_CASE;
        String name = declaration.identifierName();
        if (pattern.matcher(name).matches()) {
            return Optional.empty();
        }
        return Optional.of(
                new AnalysisFinding(
                        "Identifier '"
                                + name
                                + "' does not follow "
                                + requiredCase
                                + " naming convention",
                        declaration.start(),
                        declaration.end()));
    }
}
