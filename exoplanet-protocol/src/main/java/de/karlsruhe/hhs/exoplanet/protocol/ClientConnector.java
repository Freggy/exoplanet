package de.karlsruhe.hhs.exoplanet.protocol;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * @author Yannic Rieger
 */
public class ClientConnector {

    public Queue<Packet> getPendingPackets() {
        return this.pendingPackets;
    }

    private final Queue<Packet> pendingPackets = new LinkedBlockingDeque<>();
    private final PacketRegistry registry = new PacketRegistry();
    private Socket socket;
    private Thread readThread;
    private BufferedWriter writer;
    InetSocketAddress address;

    public ClientConnector(final InetSocketAddress address) {
        this.address = address;
    }

    /**
     * @throws Exception
     */
    public synchronized void connectAndStartReading() {
        try {
            this.socket = new Socket(this.address.getHostName(), this.address.getPort());
            this.writer = new BufferedWriter(new OutputStreamWriter(this.socket.getOutputStream()));
        } catch (final Exception ex) {
            System.out.println("[ClientConnector] Could not bind to server.");
            ex.printStackTrace();
            return;
        }

        this.readThread = new Thread(() -> {
            try (final BufferedReader in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()))) {
                String data;
                while ((data = in.readLine()) != null) {
                    if (this.readThread.isInterrupted()) {
                        in.close();
                        return;
                    }

                    final String[] split = data.split(":");
                    final String id = split[0];
                    final Optional<Packet> packetOptional = this.registry.fromId(id);

                    if (!packetOptional.isPresent()) {
                        System.out.println("[ClientConnector] Could not find packet with id " + id);
                        continue;
                    }

                    final Packet packet = packetOptional.get();
                    packet.decode(split[1].split("|"));

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
            this.writer.write(data);
        } catch (final IOException ex) {
            System.out.println("[ClientConnector] Could not write packet.");
            ex.printStackTrace();
        }
    }

    /**
     * @throws IOException
     */
    public synchronized void disconnect() throws IOException {
        this.readThread.interrupt();
        this.writer.close();
        this.socket.close();
    }
}
