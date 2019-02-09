package de.karlsruhe.hhs.exoplanet.shared.network.protocol.robot.outbound;

import de.karlsruhe.hhs.exoplanet.shared.Position;
import de.karlsruhe.hhs.exoplanet.shared.network.protocol.Packet;
import java.util.List;

/**
 * @author Yannic Rieger
 */
public class RobotLandPacket implements Packet {

    public void setPosition(final Position position) {
        this.position = position;
    }

    private Position position;

    @Override
    public String encode() {
        return "land:" + this.position.toString();
    }

    @Override
    public void decode(final List<String> data) {

    }
}
