package de.karlsruhe.hhs.exoplanet.shared.network.protocol.robot.inbound;

import de.karlsruhe.hhs.exoplanet.shared.Measure;
import de.karlsruhe.hhs.exoplanet.shared.Position;
import de.karlsruhe.hhs.exoplanet.shared.network.protocol.Packet;
import java.util.List;

/**
 * @author Yannic Rieger
 */
public class RobotMoveAndScanResponsePacket implements Packet {

    public Position getPosition() {
        return this.position;
    }

    public Measure getMeasurement() {
        return this.measurement;
    }

    private Measure measurement;
    private Position position;

    @Override
    public String encode() {
        return "";
    }

    @Override
    public void decode(final List<String> data) {
        this.measurement = Measure.parse(data.get(0));
        this.position = Position.parse(data.get(1));
    }
}
