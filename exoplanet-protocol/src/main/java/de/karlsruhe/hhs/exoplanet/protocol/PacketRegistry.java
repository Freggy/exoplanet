package de.karlsruhe.hhs.exoplanet.protocol;

import de.karlsruhe.hhs.exoplanet.protocol.inbound.InitPacket;
import de.karlsruhe.hhs.exoplanet.protocol.inbound.RobotCrashedPacket;
import de.karlsruhe.hhs.exoplanet.protocol.inbound.RobotLandedPacket;
import de.karlsruhe.hhs.exoplanet.protocol.inbound.RobotMoveAndScanResponsePacket;
import de.karlsruhe.hhs.exoplanet.protocol.inbound.RobotMoveResponsePacket;
import de.karlsruhe.hhs.exoplanet.protocol.inbound.RobotRotateResponsePacket;
import de.karlsruhe.hhs.exoplanet.protocol.inbound.RobotScanResponsePacket;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author Yannic Rieger
 */
public class PacketRegistry {

    private final Map<String, Class<? extends Packet>> packets = new HashMap<>();

    PacketRegistry() {
        this.packets.put("init", InitPacket.class);
        this.packets.put("landed", RobotLandedPacket.class);
        this.packets.put("scaned", RobotScanResponsePacket.class);
        this.packets.put("moved", RobotMoveResponsePacket.class);
        this.packets.put("mvscaned", RobotMoveAndScanResponsePacket.class);
        this.packets.put("rotated", RobotRotateResponsePacket.class);
        this.packets.put("crashed", RobotCrashedPacket.class);
    }

    public Optional<Packet> fromId(final String id) {
        final Class<? extends Packet> packet = this.packets.get(id);
        if (packet == null) return Optional.empty();
        try {
            return Optional.of(packet.newInstance());
        } catch (final Exception ex) {
            ex.printStackTrace();
        }
        return Optional.empty();
    }
}
