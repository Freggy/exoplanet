package de.karlsruhe.hhs.exoplanet.shared.network.protocol.robot.inbound;

import de.karlsruhe.hhs.exoplanet.shared.network.protocol.Packet;
import java.util.List;

/**
 * @author Yannic Rieger
 */
public class FieldBlockedResponsePacket implements Packet {

    public boolean isBlocked() {
        return this.isBlocked;
    }

    public void setBlocked(final boolean blocked) {
        this.isBlocked = blocked;
    }

    private boolean isBlocked;

    @Override
    public String encode() {
        return "isblocked:" + this.isBlocked;
    }

    @Override
    public void decode(final List<String> data) {
        this.isBlocked = Boolean.getBoolean(data.get(0));
    }
}
