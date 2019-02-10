package de.karlsruhe.hhs.exoplanet.shared.network.protocol.robot.inbound;

import de.karlsruhe.hhs.exoplanet.shared.Direction;
import de.karlsruhe.hhs.exoplanet.shared.network.protocol.Packet;
import java.util.List;

/**
 * @author Yannic Rieger
 */
public class RobotRotateResponsePacket implements Packet {

    public Direction getDirection() {
        return direction;
    }

    private Direction direction;

    @Override
    public String encode() {
        return null;
    }

    @Override
    public void decode(final List<String> data) {
        this.direction = Direction.valueOf(data.get(0));
    }
}
