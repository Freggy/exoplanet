package de.karlsruhe.hhs.exoplanet.station;

import de.karlsruhe.hhs.exoplanet.shared.Console;
import de.karlsruhe.hhs.exoplanet.shared.network.SocketReader;
import de.karlsruhe.hhs.exoplanet.shared.network.protocol.Packet;
import de.karlsruhe.hhs.exoplanet.shared.network.protocol.outbound.MeasurementPacket;
import de.karlsruhe.hhs.exoplanet.shared.network.protocol.outbound.RobotPositionUpdatePacket;
import java.io.BufferedWriter;
import java.net.Socket;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TransferQueue;

/**
 * @author Yannic Rieger
 */
public class RobotConnection extends Thread {

    private final UUID id;
    private final Socket socket;
    private BufferedWriter writer;
    private final SocketReader reader;
    private final BlockingQueue<Packet> packets = new LinkedTransferQueue<>();


    public RobotConnection(final Console console, final UUID id, final Socket socket) {
        this.id = id;
        this.socket = socket;
        this.reader = new SocketReader(null, socket, (TransferQueue<Packet>) this.packets);
    }

    @Override
    public void run() {
        this.reader.start();
        while (!this.isInterrupted()) {
            try {
                final Packet received = this.packets.take();

                if (received instanceof RobotPositionUpdatePacket) {

                } else if (received instanceof MeasurementPacket) {

                }
            } catch (final InterruptedException e) {
                this.interrupt();
            }
        }
    }

    public void write(final Packet packet) {
        packet.encode();

    }
}
