package de.karlsruhe.hhs.exoplanet.shared.network.protocol.outbound;

import de.karlsruhe.hhs.exoplanet.shared.network.protocol.Packet;
import java.util.List;

/**
 * @author Yannic Rieger
 */
public class RobotPositionUpdatePacket implements Packet {

    @Override
    public String encode() {
        return null;
    }

    @Override
    public void decode(final List<String> data) {

    }
}
