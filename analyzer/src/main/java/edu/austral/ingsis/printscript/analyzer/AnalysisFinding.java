package edu.austral.ingsis.printscript.analyzer;

import edu.austral.ingsis.printscript.common.Position;

/** A single rule violation found in the source, with its exact location. */
public record AnalysisFinding(String message, Position start, Position end) {}
