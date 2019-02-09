package de.karlsruhe.hhs.exoplanet.shared.network.protocol.robot.inbound;

import de.karlsruhe.hhs.exoplanet.shared.network.protocol.Packet;
import java.util.List;
import java.util.UUID;

/**
 * @author Yannic Rieger
 */
public class StationInfoPacket implements Packet {

    public UUID getUuid() {
        return this.uuid;
    }

    public void setUuid(final UUID uuid) {
        this.uuid = uuid;
    }

    private UUID uuid;

    @Override
    public String encode() {
        return "info:" + this.uuid.toString();
    }

    @Override
    public void decode(final List<String> data) {
        this.uuid = UUID.fromString(data.get(0));
    }
}
