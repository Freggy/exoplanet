package de.karlsruhe.hhs.exoplanet.robot;

import de.karlsruhe.hhs.exoplanet.shared.Console;
import de.karlsruhe.hhs.exoplanet.shared.Direction;
import de.karlsruhe.hhs.exoplanet.shared.Rotation;
import de.karlsruhe.hhs.exoplanet.shared.StartupOptions;
import java.net.InetSocketAddress;
import org.apache.commons.cli.Option;

/**
 * @author Yannic Rieger
 */
public class Main {

    public static void main(final String[] args) {

        /*
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
        }*/


        final Option planet = new Option("p", "planet", true, "<hostname>:<port>");
        planet.setRequired(true);

        final Option station = new Option("s", "station", true, "<hostname>:<port>");
        station.setRequired(true);

        StartupOptions.register(planet, station);
        StartupOptions.parse("ExoRobot", args);

        final Console console = new Console();

        final ExoRobot robot = new ExoRobot(
            console,
            addressFromStringArray(StartupOptions.getCmd().getOptionValue("station").split(":")),
            addressFromStringArray(StartupOptions.getCmd().getOptionValue("planet").split(":"))
        );

        robot.start();

        while (true) {

            final StringBuilder builder = new StringBuilder();
            builder.append("[");
            builder.append(robot.getCurrentPosition().getX());
            builder.append(",");
            builder.append(robot.getCurrentPosition().getY());
            builder.append(",");
            builder.append(robot.getCurrentPosition().getDir());
            builder.append("] > ");

            final String instruction = console.getReader().readLine(builder.toString());

            if (instruction.equalsIgnoreCase("exit")) {
                console.println("Destroying ExoRobot...");
                robot.destroy();
                System.exit(0);
            }

            final String[] parts = instruction.split(" ");

            if (parts[0].equalsIgnoreCase("move")) {
                if (!robot.hasLanded()) {
                    console.println("FEHLER: Roboter ist noch nicht gelandet.");
                    return;
                }
                robot.move(false);
            } else if (parts[0].equalsIgnoreCase("mvscan")) {
                if (!robot.hasLanded()) {
                    console.println("FEHLER: Roboter ist noch nicht gelandet.");
                    return;
                }
                robot.move(true);
            } else if (parts[0].equalsIgnoreCase("land")) {
                doLand(robot, console, parts);
            } else if (parts[0].equalsIgnoreCase("rotate")) {
                doRotate(robot, console, parts);
            } else {
                console.println("FEHLER: Nicht erkannter Befehl");
            }
        }
    }

    private static InetSocketAddress addressFromStringArray(final String[] data) {
        return new InetSocketAddress(data[0], Integer.valueOf(data[1]));
    }


    private static void doRotate(final ExoRobot robot, final Console console, final String[] parts) {
        if (parts.length < 2) {
            console.println("FEHLER: rotate <left|right>");
            return;
        }

        if (!robot.hasLanded()) {
            console.println("FEHLER: Roboter ist noch nicht gelandet.");
            return;
        }

        try {
            final Rotation rotation = Rotation.valueOf(parts[1].toUpperCase());
            robot.rotate(rotation);
        } catch (final Exception ex) {
            console.println("FEHLER: Ungültige eingabe.");
        }
    }

    private static void doLand(final ExoRobot robot, final Console console, final String[] parts) {
        if (robot.hasLanded()) {
            console.println("FEHLER: Roboter bereits gelandet.");
            return;
        }

        if (parts.length < 2) {
            console.println("FEHLER: land <x> <y> <dir>");
            return;
        }

        try {
            final int x = Integer.valueOf(parts[1]);
            final int y = Integer.valueOf(parts[2]);
            final Direction direction = Direction.valueOf(parts[3].toUpperCase());

            if (x < 0 || x >= robot.getFieldSize().getWidth()) {
                console.println("Eingabe für Width muss zwischen 0 und " + (robot.getFieldSize().getWidth() - 1) + " liegen.");
                return;
            }

            if (y < 0 || y >= robot.getFieldSize().getHeight()) {
                console.println("Eingabe für Width muss zwischen 0 und " + (robot.getFieldSize().getHeight() - 1) + " liegen.");
                return;
            }

            robot.land(x, y, direction);
        } catch (final Exception ex) {
            console.println("FEHLER: Ungültige eingabe.");
        }
    }
}
