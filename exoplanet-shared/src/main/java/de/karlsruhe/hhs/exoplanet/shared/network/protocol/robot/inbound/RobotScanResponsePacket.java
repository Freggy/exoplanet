package de.karlsruhe.hhs.exoplanet.shared.network.protocol.robot.inbound;

import de.karlsruhe.hhs.exoplanet.shared.Measure;
import de.karlsruhe.hhs.exoplanet.shared.network.protocol.Packet;
import java.util.List;

/**
 * @author Yannic Rieger
 */
public class RobotScanResponsePacket implements Packet {

    public Measure getMeasurement() {
        return this.measurement;
    }

    private Measure measurement;

    @Override
    public String encode() {
        return null;
    }

    @Override
    public void decode(final List<String> data) {
        this.measurement = Measure.parse(data.get(0));
    }
}
