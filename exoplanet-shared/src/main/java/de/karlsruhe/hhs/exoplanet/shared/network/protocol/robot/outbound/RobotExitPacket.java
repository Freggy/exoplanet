package de.karlsruhe.hhs.exoplanet.shared.network.protocol.robot.outbound;

import de.karlsruhe.hhs.exoplanet.shared.network.protocol.Packet;
import java.util.List;

/**
 * @author Yannic Rieger
 */
public class RobotExitPacket implements Packet {

    @Override
    public String encode() {
        return "exit";
    }

    @Override
    public void decode(final List<String> data) {

    }
}
