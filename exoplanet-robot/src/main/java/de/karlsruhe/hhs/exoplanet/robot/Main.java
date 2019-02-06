package de.karlsruhe.hhs.exoplanet.robot;

import java.net.InetSocketAddress;
import java.util.Scanner;
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
public class Main {

    public static void main(final String[] args) {

        final Options options = new Options();

        final Option planet = new Option("p", "planet", true, "<hostname>:<port>");
        planet.setRequired(true);
        options.addOption(planet);

        final Option station = new Option("s", "station", true, "<hostname>:<port>");
        station.setRequired(true);
        options.addOption(station);

        final CommandLineParser parser = new DefaultParser();
        final HelpFormatter formatter = new HelpFormatter();
        final CommandLine cmd;

        try {
            cmd = parser.parse(options, args);
        } catch (final ParseException e) {
            formatter.printHelp("ExoRobot", options);
            return;
        }

        final ExoRobot robot = new ExoRobot(
            addressFromStringArray(cmd.getOptionValue("station").split(":")),
            addressFromStringArray(cmd.getOptionValue("planet").split(":"))
        );

        robot.start();
        final Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print(">> ");
            final String instruction = scanner.nextLine();

            if (instruction.equalsIgnoreCase("exit")) {
                System.out.println("Destroying ExoRobot...");
                robot.destroy();
                System.exit(0);
            }

            if (instruction.startsWith("move ")) {
                // TODO: move robot in dir
            }

        }
    }

    private static InetSocketAddress addressFromStringArray(final String[] data) {
        return new InetSocketAddress(data[0], Integer.valueOf(data[1]));
    }
}
