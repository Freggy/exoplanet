package de.karlsruhe.hhs.exoplanet.shared.network.protocol.robot.outbound;

import de.karlsruhe.hhs.exoplanet.shared.Position;
import de.karlsruhe.hhs.exoplanet.shared.network.protocol.Packet;
import java.util.List;

/**
 * @author Yannic Rieger
 */
public class FieldBlockedRequestPacket implements Packet {

    public Position getPosition() {
        return this.position;
    }

    public void setPosition(final Position position) {
        this.position = position;
    }

    private Position position;

    @Override
    public String encode() {
        return "fieldblockedrequest:" + this.position.toString();
    }

    @Override
    public void decode(final List<String> data) {
        this.position = Position.parse(data.get(0));
    }
}
