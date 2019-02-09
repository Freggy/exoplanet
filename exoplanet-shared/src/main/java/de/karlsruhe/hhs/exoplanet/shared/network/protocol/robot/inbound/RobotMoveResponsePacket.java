package de.karlsruhe.hhs.exoplanet.shared.network.protocol.robot.inbound;

import de.karlsruhe.hhs.exoplanet.shared.Position;
import de.karlsruhe.hhs.exoplanet.shared.network.protocol.Packet;
import java.util.List;

/**
 * @author Yannic Rieger
 */
public class RobotMoveResponsePacket implements Packet {

    public Position getPosition() {
        return this.position;
    }

    private Position position;

    @Override
    public String encode() {
        return null;
    }

    @Override
    public void decode(final List<String> data) {
        this.position = Position.parse(data.get(0));
    }
}
