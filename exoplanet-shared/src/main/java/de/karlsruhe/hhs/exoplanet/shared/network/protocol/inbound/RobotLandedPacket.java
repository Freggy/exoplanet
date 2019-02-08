package de.karlsruhe.hhs.exoplanet.shared.network.protocol.inbound;

import de.karlsruhe.hhs.exoplanet.shared.Measure;
import de.karlsruhe.hhs.exoplanet.shared.network.protocol.Packet;
import java.util.List;

/**
 * @author Yannic Rieger
 */
public class RobotLandedPacket implements Packet {

    public Measure getMeasurement() {
        return this.measurement;
    }

    private Measure measurement;

    @Override
    public String encode() {
        return "";
    }

    @Override
    public void decode(final List<String> data) {
        this.measurement = Measure.parse(data.get(0));
    }
}
