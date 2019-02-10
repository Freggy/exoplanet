package de.karlsruhe.hhs.exoplanet.shared.network.protocol.robot.outbound;

import de.karlsruhe.hhs.exoplanet.shared.Rotation;
import de.karlsruhe.hhs.exoplanet.shared.network.protocol.Packet;
import java.util.List;

/**
 * @author Yannic Rieger
 */
public class RobotRotatePacket implements Packet {

    public void setRotation(final Rotation rotation) {
        this.rotation = rotation;
    }

    private Rotation rotation;

    @Override
    public String encode() {
        return "rotate:" + this.rotation.name();
    }

    @Override
    public void decode(final List<String> data) {

    }
}
