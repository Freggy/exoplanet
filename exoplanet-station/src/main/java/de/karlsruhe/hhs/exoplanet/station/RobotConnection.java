package de.karlsruhe.hhs.exoplanet.station;

import de.karlsruhe.hhs.exoplanet.shared.Measure;
import de.karlsruhe.hhs.exoplanet.shared.Position;
import de.karlsruhe.hhs.exoplanet.shared.network.SocketConsumer;
import de.karlsruhe.hhs.exoplanet.shared.network.SocketReader;
import de.karlsruhe.hhs.exoplanet.shared.network.protocol.Packet;
import de.karlsruhe.hhs.exoplanet.shared.network.protocol.bidirectional.RobotPositionUpdatePacket;
import de.karlsruhe.hhs.exoplanet.shared.network.protocol.robot.inbound.FieldBlockedResponsePacket;
import de.karlsruhe.hhs.exoplanet.shared.network.protocol.robot.inbound.StationInfoPacket;
import de.karlsruhe.hhs.exoplanet.shared.network.protocol.robot.outbound.FieldBlockedRequestPacket;
import de.karlsruhe.hhs.exoplanet.shared.network.protocol.robot.outbound.MeasurementPacket;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TransferQueue;

/**
 * @author Yannic Rieger
 */
public class RobotConnection {

    private final UUID id;
    private final Socket socket;
    private BufferedWriter writer;
    private final SocketReader reader;
    private final SocketConsumer consumer;
    private final ExecutorService executorService;
    private final Map<UUID, Position> positions;
    private final Map<Position, Measure> field;

    public RobotConnection(
        final ExecutorService executorService,
        final Map<UUID, Position> positions,
        final Map<Position, Measure> field,
        final UUID id,
        final Socket socket
    ) {
        this.id = id;
        this.socket = socket;
        final BlockingQueue<Packet> packets = new LinkedTransferQueue<>();
        this.reader = new SocketReader(null, socket, (TransferQueue<Packet>) packets);
        this.consumer = new SocketConsumer(packets);
        this.executorService = executorService;
        this.positions = positions;
        this.field = field;
        try {
            this.writer = new BufferedWriter(new OutputStreamWriter(this.socket.getOutputStream()));
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void write(final Packet packet) {
        final String data = packet.encode();
        try {
            this.writer.write(data + "\n"); // Append \n because we need to send a new line after each payload
            this.writer.flush();
        } catch (final IOException ex) {
            ex.printStackTrace();
        }
    }

    public void connect() {
        final StationInfoPacket infoPacket = new StationInfoPacket();
        infoPacket.setUuid(this.id);
        this.write(infoPacket);

        this.consumer.consume(MeasurementPacket.class, packet -> {
            this.executorService.submit(() -> {
                // TODO: write to database
            });
        }).consume(RobotPositionUpdatePacket.class, packet -> {
            // TODO: output
            // TODO: update field
            this.positions.put(packet.getRobotId(), packet.getPosition());
        }).consume(FieldBlockedRequestPacket.class, packet -> {
            // TODO: output
            final FieldBlockedResponsePacket response = new FieldBlockedResponsePacket();
            response.setBlocked(this.positions.containsValue(packet.getPosition()));
            this.write(response);
        }).consume(MeasurementPacket.class, packet -> {
            // TODO: output
            this.field.put(packet.getPosition(), packet.getMeasurement());
        });

        this.reader.start();
        this.consumer.start();
    }

    public synchronized void close() {
        try {
            this.consumer.shutdown();
            this.reader.shutdown();
            this.writer.close();
            this.socket.close();
        } catch (final Exception ex) {
            ex.printStackTrace();
        }
    }
}
