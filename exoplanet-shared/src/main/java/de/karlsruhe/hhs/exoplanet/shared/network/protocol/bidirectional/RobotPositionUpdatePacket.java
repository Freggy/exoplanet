package de.karlsruhe.hhs.exoplanet.shared.network.protocol.bidirectional;

import de.karlsruhe.hhs.exoplanet.shared.Position;
import de.karlsruhe.hhs.exoplanet.shared.network.protocol.Packet;
import java.util.List;
import java.util.UUID;

/**
 * @author Yannic Rieger
 */
public class RobotPositionUpdatePacket implements Packet {

    public UUID getRobotId() {
        return this.robotId;
    }

    public Position getPosition() {
        return this.position;
    }

    public void setRobotId(final UUID robotId) {
        this.robotId = robotId;
    }

    public void setPosition(final Position position) {
        this.position = position;
    }

    private UUID robotId;
    private Position position;

    @Override
    public String encode() {
        return "posupdate:" + this.robotId.toString() + ":" + this.position.toString();
    }

    @Override
    public void decode(final List<String> data) {
        this.robotId = UUID.fromString(data.get(0));
        this.position = Position.parse(data.get(1));
    }
}
