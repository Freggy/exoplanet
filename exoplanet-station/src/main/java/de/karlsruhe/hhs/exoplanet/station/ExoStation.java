package de.karlsruhe.hhs.exoplanet.station;

import de.karlsruhe.hhs.exoplanet.shared.Console;
import de.karlsruhe.hhs.exoplanet.shared.Measure;
import de.karlsruhe.hhs.exoplanet.shared.Position;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Yannic Rieger
 */
public class ExoStation {

    private ServerSocket serverSocket;
    private Thread acceptThread;
    private final Map<UUID, RobotConnection> connections;
    private final Map<UUID, Position> positions;
    private final Map<Position, Measure> field;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Console console;
    private final DataAccess access;

    public ExoStation(final DataAccess access, final int port, final Console console) {
        this.console = console;
        this.connections = new ConcurrentHashMap<>();
        this.positions = new ConcurrentHashMap<>();
        this.field = new ConcurrentHashMap<>();
        this.access = access;

        try {
            this.serverSocket = new ServerSocket(port);
            // TODO: maybe set read-timeout
        } catch (final IOException e) {
            e.printStackTrace();
        }

        this.acceptThread = new Thread(() -> {
            while (!this.acceptThread.isInterrupted()) {
                try {
                    final Socket client = this.serverSocket.accept();
                    client.setSoTimeout(6000);
                    final UUID id = UUID.randomUUID();
                    this.console.println("[ExoStation] New connection: " + id);
                    final RobotConnection connection = new RobotConnection(
                        this.console,
                        this.executorService,
                        this.positions,
                        this.field,
                        this.connections,
                        id,
                        this.access,
                        client
                    );
                    this.connections.put(id, connection);
                    connection.connect();
                } catch (final IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void start() {
        this.console.println("[ExoStation] Starting...");
        this.access.connect();
        this.acceptThread.start();
    }

    public void shutdown() {
        this.connections.values().forEach(RobotConnection::close);
        this.access.close();
        this.acceptThread.interrupt();
        this.executorService.shutdown();
    }
}
