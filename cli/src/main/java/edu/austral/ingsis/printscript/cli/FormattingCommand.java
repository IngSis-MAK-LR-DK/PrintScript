package edu.austral.ingsis.printscript.cli;

import java.io.IOException;
import java.util.Iterator;

import edu.austral.ingsis.printscript.common.ast.Statement;
import edu.austral.ingsis.printscript.config.ConfigLoader;
import edu.austral.ingsis.printscript.formatter.Formatter;
import edu.austral.ingsis.printscript.formatter.FormatterConfig;

final class FormattingCommand implements Command {

    private final Pipeline pipeline;
    private final Formatter formatter;
    private final ConfigLoader<FormatterConfig> configLoader;

    FormattingCommand(
            Pipeline pipeline, Formatter formatter, ConfigLoader<FormatterConfig> configLoader) {
        this.pipeline = pipeline;
        this.formatter = formatter;
        this.configLoader = configLoader;
    }

    @Override
    public int run(CliArguments arguments) throws IOException {
        Iterator<Statement> statements = pipeline.parse(arguments.sourceFile());
        FormatterConfig config =
                arguments.configFile().isPresent()
                        ? ConfigFiles.load(arguments.configFile().get(), configLoader)
                        : FormatterConfig.defaultConfig();
        System.out.print(formatter.format(statements, config));
        return 0;
    }
}
