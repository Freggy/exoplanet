package de.karlsruhe.hhs.exoplanet.robot;

import de.karlsruhe.hhs.exoplanet.shared.Console;
import de.karlsruhe.hhs.exoplanet.shared.network.protocol.Packet;
import de.karlsruhe.hhs.exoplanet.shared.network.protocol.PacketRegistry;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * @author Yannic Rieger
 */
public class ClientConnector {

    public BlockingQueue<Packet> getPendingPackets() {
        return this.pendingPackets;
    }

    private final BlockingQueue<Packet> pendingPackets = new LinkedBlockingDeque<>();
    private final PacketRegistry registry = new PacketRegistry();
    private Socket socket;
    private Thread readThread;
    private BufferedWriter writer;
    private final InetSocketAddress address;
    private final Console console;

    public ClientConnector(final Console console, final InetSocketAddress address) {
        this.address = address;
        this.console = console;
    }

    /**
     *
     */
    public synchronized void connectAndStartReading() {
        this.console.println("[ClientConnector] Connecting to " + this.address.getHostString() + ":" + this.address.getPort());

        try {
            this.socket = new Socket(this.address.getHostName(), this.address.getPort());
            this.socket.setSoTimeout(6000);
            this.writer = new BufferedWriter(new OutputStreamWriter(this.socket.getOutputStream()));
        } catch (final Exception ex) {
            this.console.println("[ClientConnector] Could not bind to server.");
            ex.printStackTrace();
            return;
        }

        this.readThread = new Thread(() -> {
            try (final BufferedReader in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()))) {
                while (!this.readThread.isInterrupted()) {
                    String payload;

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

                    this.pendingPackets.add(packet);
                }
            } catch (final Exception ex) {

                ex.printStackTrace();
            }

        });
        this.readThread.start();
    }


    /**
     * @param packet
     */
    public synchronized void write(final Packet packet) {
        final String data = packet.encode();
        try {
            this.writer.write(data + "\n"); // Append \n because we need to send a new line after each payload
            this.writer.flush();
        } catch (final IOException ex) {
            this.console.println("[ClientConnector] Could not write packet.");
            ex.printStackTrace();
        }
    }


    /**
     *
     */
    public synchronized void disconnect() {
        this.readThread.interrupt();
        try {
            // Wait for thread to die, otherwise Exception is thrown because
            // the reader tries to read from the closed socket.
            this.readThread.join();
            this.writer.close();
            this.socket.close();
        } catch (final Exception ex) {
            ex.printStackTrace();
        }
    }
}
