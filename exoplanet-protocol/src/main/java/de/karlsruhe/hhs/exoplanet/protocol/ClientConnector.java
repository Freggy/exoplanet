package de.karlsruhe.hhs.exoplanet.protocol;

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
                        System.out.println("[ClientConnector] Could not find packet with id " + id);
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
