package de.karlsruhe.hhs.exoplanet.shared;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * @author Yannic Rieger
 */
public class StartupOptions {

    public static CommandLine getCmd() {
        return cmd;
    }

    private static final CommandLineParser PARSER = new DefaultParser();
    private static final HelpFormatter HELP_FORMATTER = new HelpFormatter();
    private static CommandLine cmd = null;
    private static final Options OPTIONS = new Options();

    public static void register(final Option... options) {
        for (final Option option : options) {
            OPTIONS.addOption(option);
        }
    }

    public static void parse(final String name, final String[] args) {
        try {
            cmd = PARSER.parse(OPTIONS, args);
        } catch (final ParseException e) {
            HELP_FORMATTER.printHelp(name, OPTIONS);
            return;
        }
    }

}
