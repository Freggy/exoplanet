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
            this.writer = new BufferedWriter(new OutputStreamWriter(this.socket.getOutputStream()));
        } catch (final Exception ex) {
            this.console.println("[ClientConnector] Could not bind to server.");
            ex.printStackTrace();
            return;
        }

        this.readThread = new Thread(() -> {
            try (final BufferedReader in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()))) {
                String payload;
                while ((payload = in.readLine()) != null) {
                    if (this.readThread.isInterrupted()) {
                        in.close();
                        return;
                    }

                    final String[] split = payload.split(":");
                    final String id = split[0];
                    final Optional<Packet> packetOptional = this.registry.fromId(id);

                    if (!packetOptional.isPresent()) {
                        this.console.println("[ClientConnector] Could not find packet with id " + id);
                        continue;
                    }

                    final List<String> complete;

                    if (split.length > 2) {
                        // We need to have this edge case here because
                        // one payload is formatted as follows:
                        // mvscaned:Measure|Ground|temp:POSITION|x|y|direction
                        // so the normal way of splitting the string does not work.

                        // After splitting it once our payload looks like this:
                        // [0] = mvscanned
                        // [1] = Measure|Ground|temp
                        // [2] = POSITION|x|y|direction
                        // So in order to retrieve the actual payload we have to split [1] and [2] at "|".

                        complete = new ArrayList<>();
                        complete.addAll(Arrays.asList(split[1].split("\\|"))); // Contains Measure|Ground|temp
                        complete.addAll(Arrays.asList(split[2].split("\\|"))); // POSITION|x|y|direction
                    } else {
                        complete = Arrays.asList(split[1].split("\\|"));
                    }

                    final Packet packet = packetOptional.get();
                    packet.decode(complete);

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
            this.writer.close();
            this.socket.close();
        } catch (final IOException ex) {
            ex.printStackTrace();
        }
    }
}
