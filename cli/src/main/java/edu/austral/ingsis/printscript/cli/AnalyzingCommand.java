package edu.austral.ingsis.printscript.cli;

import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;

import edu.austral.ingsis.printscript.analyzer.AnalysisFinding;
import edu.austral.ingsis.printscript.analyzer.Analyzer;
import edu.austral.ingsis.printscript.analyzer.AnalyzerConfig;
import edu.austral.ingsis.printscript.common.ast.Statement;
import edu.austral.ingsis.printscript.config.ConfigLoader;

final class AnalyzingCommand implements Command {

    private final Pipeline pipeline;
    private final Analyzer analyzer;
    private final ConfigLoader<AnalyzerConfig> configLoader;

    AnalyzingCommand(
            Pipeline pipeline, Analyzer analyzer, ConfigLoader<AnalyzerConfig> configLoader) {
        this.pipeline = pipeline;
        this.analyzer = analyzer;
        this.configLoader = configLoader;
    }

    @Override
    public int run(CliArguments arguments) throws IOException {
        Iterator<Statement> statements = pipeline.parse(arguments.sourceFile());
        AnalyzerConfig config =
                arguments.configFile().isPresent()
                        ? ConfigFiles.load(arguments.configFile().get(), configLoader)
                        : AnalyzerConfig.defaultConfig();

        AtomicBoolean foundViolations = new AtomicBoolean(false);
        analyzer.analyze(
                statements,
                config,
                finding -> {
                    foundViolations.set(true);
                    print(finding);
                });

        if (foundViolations.get()) {
            return 1;
        }
        System.out.println("OK: no rule violations found");
        return 0;
    }

    private static void print(AnalysisFinding finding) {
        System.out.printf(
                "[%d:%d - %d:%d] %s%n",
                finding.start().line(),
                finding.start().column(),
                finding.end().line(),
                finding.end().column(),
                finding.message());
    }
}
