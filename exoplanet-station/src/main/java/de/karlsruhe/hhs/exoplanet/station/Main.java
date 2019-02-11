package de.karlsruhe.hhs.exoplanet.station;

import de.karlsruhe.hhs.exoplanet.shared.Console;
import de.karlsruhe.hhs.exoplanet.shared.StartupOptions;
import org.apache.commons.cli.Option;

/**
 * @author Yannic Rieger
 */
public class Main {

    public static void main(final String[] args) {
        final Option portOption = new Option("p", "port", true, "Port: 0 - 65535");
        portOption.setRequired(true);


        final Option data = new Option("d", "database", true, "<hostname>:<port>,<user>,<password>,<database>");
        data.setRequired(true);


        StartupOptions.register(portOption, data);
        StartupOptions.parse("ExoStation", args);

        final DataAccess access = createDataAccess(StartupOptions.getCmd().getOptionValue("database").split(","));

        if (access == null) {
            System.out.println("Ungültige Verbindungsinformationen.");
            return;
        }

        final int port = Integer.valueOf(StartupOptions.getCmd().getOptionValue("port"));

        if (port < 0 || port > 65535) {
            System.out.println("Ungültiger Port. Port muss sich zwischen 0 und 65535 befinden.");
            return;
        }

        final Console console = new Console();

        final ExoStation station = new ExoStation(access, port, console);
        station.start();

        while (true) {
            final String instruction = console.getReader().readLine("> ");

            if (instruction.equalsIgnoreCase("exit")) {
                console.println("Herunterfahren...");
                station.shutdown();
                System.exit(0);
            }
        }
    }

    private static DataAccess createDataAccess(final String[] connectionInfo) {
        if (connectionInfo.length < 4) {
            return null;
        }

        final String[] host = connectionInfo[0].split(":");

        if (host.length < 2) {
            return null;
        }

        try {
            final String hostname = host[0];
            final int port = Integer.valueOf(host[1]);
            final String user = connectionInfo[1];
            final String password = connectionInfo[2];
            final String database = connectionInfo[3];
            return new DataAccess(hostname, port, database, user, password);
        } catch (final Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
