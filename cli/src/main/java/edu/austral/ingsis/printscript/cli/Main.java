package edu.austral.ingsis.printscript.cli;

import edu.austral.ingsis.printscript.analyzer.AnalysisFinding;
import edu.austral.ingsis.printscript.analyzer.AnalyzerConfig;
import edu.austral.ingsis.printscript.analyzer.AnalyzerConfigLoader;
import edu.austral.ingsis.printscript.analyzer.PrintScriptAnalyzer;
import edu.austral.ingsis.printscript.common.PrintScriptException;
import edu.austral.ingsis.printscript.common.ast.Statement;
import edu.austral.ingsis.printscript.formatter.FormatterConfig;
import edu.austral.ingsis.printscript.formatter.FormatterConfigLoader;
import edu.austral.ingsis.printscript.formatter.PrintScriptFormatter;
import edu.austral.ingsis.printscript.interpreter.PrintScriptInterpreter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

public final class Main {

    public static void main(String[] args) {
        try {
            CliArguments arguments = CliArguments.parse(args);
            run(arguments);
        } catch (CliUsageException e) {
            System.err.println(e.getMessage());
            System.exit(2);
        } catch (PrintScriptException e) {
            printError(e);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Could not read the source file: " + e.getMessage());
            System.exit(1);
        }
    }

    private static void run(CliArguments arguments) throws IOException {
        switch (arguments.operation()) {
            case VALIDATION -> validate(arguments.sourceFile());
            case EXECUTION -> execute(arguments.sourceFile());
            case FORMATTING -> format(arguments.sourceFile(), arguments.configFile());
            case ANALYZING -> analyze(arguments.sourceFile(), arguments.configFile());
        }
    }

    private static void validate(Path sourceFile) throws IOException {
        Iterator<Statement> statements = Pipeline.parse(sourceFile);
        PrintStream discard = new PrintStream(OutputStream.nullOutputStream());
        new PrintScriptInterpreter().interpret(statements, discard);
        System.out.println("OK: no syntax or semantic errors found");
    }

    private static void execute(Path sourceFile) throws IOException {
        Iterator<Statement> statements = Pipeline.parse(sourceFile);
        new PrintScriptInterpreter().interpret(statements, System.out);
    }

    private static void format(Path sourceFile, Optional<Path> configFile) throws IOException {
        Iterator<Statement> statements = Pipeline.parse(sourceFile);
        FormatterConfig config = configFile.isPresent() ? loadFormatterConfig(configFile.get()) : FormatterConfig.defaultConfig();
        String formatted = new PrintScriptFormatter().format(statements, config);
        System.out.print(formatted);
    }

    private static void analyze(Path sourceFile, Optional<Path> configFile) throws IOException {
        Iterator<Statement> statements = Pipeline.parse(sourceFile);
        AnalyzerConfig config = configFile.isPresent() ? loadAnalyzerConfig(configFile.get()) : AnalyzerConfig.defaultConfig();
        List<AnalysisFinding> findings = new PrintScriptAnalyzer().analyze(statements, config);

        if (findings.isEmpty()) {
            System.out.println("OK: no rule violations found");
            return;
        }
        for (AnalysisFinding finding : findings) {
            System.out.printf(
                    "[%d:%d - %d:%d] %s%n",
                    finding.start().line(),
                    finding.start().column(),
                    finding.end().line(),
                    finding.end().column(),
                    finding.message());
        }
        System.exit(1);
    }

    private static FormatterConfig loadFormatterConfig(Path configFile) throws IOException {
        try (Reader reader = Files.newBufferedReader(configFile, StandardCharsets.UTF_8)) {
            return new FormatterConfigLoader().load(reader, ConfigFiles.formatOf(configFile));
        }
    }

    private static AnalyzerConfig loadAnalyzerConfig(Path configFile) throws IOException {
        try (Reader reader = Files.newBufferedReader(configFile, StandardCharsets.UTF_8)) {
            return new AnalyzerConfigLoader().load(reader, ConfigFiles.formatOf(configFile));
        }
    }

    private static void printError(PrintScriptException e) {
        System.err.println(e.getClass().getSimpleName() + ": " + e.getMessage());
        System.err.printf(
                "  at line %d, column %d to line %d, column %d%n",
                e.start().line(), e.start().column(), e.end().line(), e.end().column());
    }
}
