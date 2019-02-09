package de.karlsruhe.hhs.exoplanet.shared.network;

import de.karlsruhe.hhs.exoplanet.shared.Console;
import de.karlsruhe.hhs.exoplanet.shared.network.protocol.Packet;
import de.karlsruhe.hhs.exoplanet.shared.network.protocol.PacketRegistry;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TransferQueue;

/**
 * @author Yannic Rieger
 */
public class SocketReader extends Thread {

    private final Socket socket;
    private final PacketRegistry registry = new PacketRegistry();
    private final Console console;
    private final TransferQueue<Packet> transferQueue;

    public SocketReader(final Console console, final Socket socket, final TransferQueue<Packet> transferQueue) {
        this.socket = socket;
        this.console = console;
        this.transferQueue = transferQueue;
    }

    @Override
    public void run() {
        try (final BufferedReader in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()))) {
            String payload;
            while (!this.isInterrupted()) {
                // The socket read-timeout we have set throws an exception if no
                // payload was received after the specified amount of time.
                // We have to do this because otherwise readLine() would block the thread until
                // a payload is received and we won't be able to interrupt the thread causing the program to
                // not be able to exit.
                try {
                    payload = in.readLine();
                } catch (final IOException ex) {
                    payload = null;
                }

                if (payload == null) continue;

                final String[] split = payload.split(":");
                final String id = split[0];
                final Optional<Packet> packetOptional = this.registry.fromId(id);

                if (!packetOptional.isPresent()) {
                    this.console.println("[ClientConnector] Could not find packet with id " + id);
                    continue;
                }

                final List<String> data = new ArrayList<>(Arrays.asList(split).subList(1, split.length));
                final Packet packet = packetOptional.get();

                packet.decode(data);
                this.transferQueue.transfer(packet);
            }
        } catch (final Exception ex) {
            ex.printStackTrace();
        }
    }

}
