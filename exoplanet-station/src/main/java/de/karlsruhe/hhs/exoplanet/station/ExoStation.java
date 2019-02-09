package de.karlsruhe.hhs.exoplanet.station;

import de.karlsruhe.hhs.exoplanet.shared.FieldTile;
import de.karlsruhe.hhs.exoplanet.shared.network.protocol.Packet;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedTransferQueue;

/**
 * @author Yannic Rieger
 */
public class ExoStation {

    private final LinkedTransferQueue<Packet> sharedQueue = new LinkedTransferQueue<>();
    private ServerSocket serverSocket;
    private Thread acceptThread;
    private final Map<UUID, RobotConnection> connections;
    private FieldTile[][] field;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public ExoStation(final int port) {
        this.connections = new ConcurrentHashMap<>();

        try {
            this.serverSocket = new ServerSocket(port, 10, Inet4Address.getByName("0.0.0.0"));
            // TODO: probably have to set SO timeout as well
        } catch (final IOException e) {
            e.printStackTrace();
        }

        this.acceptThread = new Thread(() -> {
            while (!this.acceptThread.isInterrupted()) {
                try {
                    final Socket client = this.serverSocket.accept();
                    final UUID id = UUID.randomUUID();
                    //final RobotConnection connection = new RobotConnection(id, this.sharedQueue, client);
                    //this.connections.put(id, connection);
                    //connection.start();
                } catch (final IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    public void start() {
        this.acceptThread.start();
    }

    public void shutdown() {
        this.acceptThread.interrupt();
        this.executorService.shutdown();
    }
}
