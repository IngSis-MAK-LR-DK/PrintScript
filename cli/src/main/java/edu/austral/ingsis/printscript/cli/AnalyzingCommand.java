package edu.austral.ingsis.printscript.cli;

import edu.austral.ingsis.printscript.analyzer.AnalysisFinding;
import edu.austral.ingsis.printscript.analyzer.Analyzer;
import edu.austral.ingsis.printscript.analyzer.AnalyzerConfig;
import edu.austral.ingsis.printscript.common.ast.Statement;
import edu.austral.ingsis.printscript.config.ConfigLoader;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;

final class AnalyzingCommand implements Command {

    private final Pipeline pipeline;
    private final Analyzer analyzer;
    private final ConfigLoader<AnalyzerConfig> configLoader;

    AnalyzingCommand(Pipeline pipeline, Analyzer analyzer, ConfigLoader<AnalyzerConfig> configLoader) {
        this.pipeline = pipeline;
        this.analyzer = analyzer;
        this.configLoader = configLoader;
    }

    @Override
    public int run(CliArguments arguments) throws IOException {
        Iterator<Statement> statements = pipeline.parse(arguments.sourceFile());
        AnalyzerConfig config = arguments.configFile().isPresent()
                ? loadConfig(arguments.configFile().get())
                : AnalyzerConfig.defaultConfig();
        List<AnalysisFinding> findings = analyzer.analyze(statements, config);

        if (findings.isEmpty()) {
            System.out.println("OK: no rule violations found");
            return 0;
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
        return 1;
    }

    private AnalyzerConfig loadConfig(Path configFile) throws IOException {
        try (Reader reader = Files.newBufferedReader(configFile, StandardCharsets.UTF_8)) {
            return configLoader.load(reader, ConfigFiles.formatOf(configFile));
        }
    }
}
