package de.karlsruhe.hhs.exoplanet.robot;

import de.karlsruhe.hhs.exoplanet.shared.Console;
import java.net.InetSocketAddress;
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

        final Console console = new Console();

        final ExoRobot robot = new ExoRobot(
            console,
            addressFromStringArray(cmd.getOptionValue("station").split(":")),
            addressFromStringArray(cmd.getOptionValue("planet").split(":"))
        );

        robot.start();

        while (true) {
            final String instruction = console.getReader().readLine(">> ");

            if (instruction.equalsIgnoreCase("exit")) {
                console.println("Destroying ExoRobot...");
                robot.destroy();
                System.exit(0);
            }

            if (instruction.startsWith("move ")) {
                // TODO: move robot in dir
            } else if (instruction.startsWith("land ")) {

                if (robot.hasLanded()) {
                    console.println("FEHLER: Roboter bereits gelandet.");
                    continue;
                }

                final String[] parts = instruction.split(" ");

                if (parts.length < 2) {
                    console.println("FEHLER: land <x> <y>");
                    continue;
                }

                try {
                    final int x = Integer.valueOf(parts[1]);
                    final int y = Integer.valueOf(parts[2]);
                    robot.land(x, y);
                } catch (final Exception ex) {
                    console.println("FEHLER: Ungültige eingabe.");
                }
            } else {
                console.println("FEHLER: Nicht erkannter Befehl");
            }
        }
    }

    private static InetSocketAddress addressFromStringArray(final String[] data) {
        return new InetSocketAddress(data[0], Integer.valueOf(data[1]));
    }
}
