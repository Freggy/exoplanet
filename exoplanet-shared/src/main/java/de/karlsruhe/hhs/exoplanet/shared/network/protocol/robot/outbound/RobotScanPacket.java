package de.karlsruhe.hhs.exoplanet.shared.network.protocol.robot.outbound;

import de.karlsruhe.hhs.exoplanet.shared.network.protocol.Packet;
import java.util.List;

/**
 * @author Yannic Rieger
 */
public class RobotScanPacket implements Packet {

    @Override
    public String encode() {
        return "scan";
    }

    @Override
    public void decode(final List<String> data) {

    }
}
