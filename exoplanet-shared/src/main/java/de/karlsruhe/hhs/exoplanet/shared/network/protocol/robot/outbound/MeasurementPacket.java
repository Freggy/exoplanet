package de.karlsruhe.hhs.exoplanet.shared.network.protocol.robot.outbound;

import de.karlsruhe.hhs.exoplanet.shared.Measure;
import de.karlsruhe.hhs.exoplanet.shared.Position;
import de.karlsruhe.hhs.exoplanet.shared.network.protocol.Packet;
import java.util.List;

/**
 * @author Yannic Rieger
 */
public class MeasurementPacket implements Packet {

    public Position getPosition() {
        return this.position;
    }

    public Measure getMeasurement() {
        return this.measurement;
    }

    public void setMeasurement(final Measure measurement) {
        this.measurement = measurement;
    }

    public void setPosition(final Position position) {
        this.position = position;
    }

    private Position position;
    private Measure measurement;

    @Override
    public String encode() {
        return null;
    }

    @Override
    public void decode(final List<String> data) {

    }
}
