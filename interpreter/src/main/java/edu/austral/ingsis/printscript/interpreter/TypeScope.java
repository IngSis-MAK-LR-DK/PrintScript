package edu.austral.ingsis.printscript.interpreter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edu.austral.ingsis.printscript.common.SemanticException;
import edu.austral.ingsis.printscript.common.SourceRange;

/**
 * What static validation knows about each variable at a point in the program. Unlike {@link
 * Environment} it holds no values, only types and how sure we are about them.
 *
 * <p>After an {@code if}, the two branches are combined with {@link #mergedWith}. Which branch runs
 * is unknown, and blocks don't open a new scope, so a variable touched by only one branch might or
 * might not be there. Validation gives those the benefit of the doubt: it must never reject a
 * program that could run fine.
 */
final class TypeScope {

    /** Type of a variable whose two {@code if} branches declared it differently. */
    static final String UNKNOWN_TYPE = "unknown";

    private record Variable(
            String type, boolean isConstant, boolean isAssigned, boolean isCertainlyDeclared) {}

    private final Map<String, Variable> variables;

    TypeScope() {
        this(new HashMap<>());
    }

    private TypeScope(Map<String, Variable> variables) {
        this.variables = variables;
    }

    TypeScope copy() {
        return new TypeScope(new HashMap<>(variables));
    }

    void declare(
            String name, String type, boolean isConstant, SourceRange nameAt, SourceRange typeAt) {
        Variable existing = variables.get(name);
        if (existing != null && existing.isCertainlyDeclared()) {
            throw error("Variable '" + name + "' is already declared", nameAt);
        }
        if (!Environment.isKnownType(type)) {
            throw error("Unknown type '" + type + "'", typeAt);
        }
        variables.put(name, new Variable(type, isConstant, false, true));
    }

    /** The declared type of {@code name}, which only has to exist - it may still be unassigned. */
    String typeOf(String name, SourceRange at) {
        return require(name, at).type();
    }

    /** The declared type of {@code name}, for a place that needs its value. */
    String typeForReading(String name, SourceRange at) {
        Variable variable = require(name, at);
        if (!variable.isAssigned()) {
            throw error("Variable '" + name + "' is used before being assigned", at);
        }
        return variable.type();
    }

    void markAssigned(String name, SourceRange at) {
        Variable variable = require(name, at);
        if (variable.isConstant() && variable.isAssigned()) {
            throw error("Cannot reassign constant '" + name + "'", at);
        }
        variables.put(
                name,
                new Variable(
                        variable.type(),
                        variable.isConstant(),
                        true,
                        variable.isCertainlyDeclared()));
    }

    /** Replaces this scope with what's known after running either one of two branches. */
    void mergedWith(TypeScope thenBranch, TypeScope elseBranch) {
        Set<String> names = new HashSet<>(thenBranch.variables.keySet());
        names.addAll(elseBranch.variables.keySet());
        variables.clear();
        for (String name : names) {
            variables.put(
                    name, merge(thenBranch.variables.get(name), elseBranch.variables.get(name)));
        }
    }

    private static Variable merge(Variable first, Variable second) {
        if (first == null || second == null) {
            Variable only = first != null ? first : second;
            return new Variable(only.type(), only.isConstant(), only.isAssigned(), false);
        }
        return new Variable(
                first.type().equals(second.type()) ? first.type() : UNKNOWN_TYPE,
                first.isConstant() && second.isConstant(),
                first.isAssigned() || second.isAssigned(),
                first.isCertainlyDeclared() && second.isCertainlyDeclared());
    }

    private Variable require(String name, SourceRange at) {
        Variable variable = variables.get(name);
        if (variable == null) {
            throw error("Variable '" + name + "' is not declared", at);
        }
        return variable;
    }

    private static SemanticException error(String message, SourceRange at) {
        return new SemanticException(message, at.start(), at.end());
    }
}
