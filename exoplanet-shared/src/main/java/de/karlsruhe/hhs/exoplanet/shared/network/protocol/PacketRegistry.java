package de.karlsruhe.hhs.exoplanet.shared.network.protocol;

import de.karlsruhe.hhs.exoplanet.shared.network.protocol.robot.inbound.FieldBlockedResponsePacket;
import de.karlsruhe.hhs.exoplanet.shared.network.protocol.robot.inbound.InitPacket;
import de.karlsruhe.hhs.exoplanet.shared.network.protocol.robot.inbound.RobotCrashedPacket;
import de.karlsruhe.hhs.exoplanet.shared.network.protocol.robot.inbound.RobotLandedPacket;
import de.karlsruhe.hhs.exoplanet.shared.network.protocol.robot.inbound.RobotMoveAndScanResponsePacket;
import de.karlsruhe.hhs.exoplanet.shared.network.protocol.robot.inbound.RobotMoveResponsePacket;
import de.karlsruhe.hhs.exoplanet.shared.network.protocol.robot.inbound.RobotRotateResponsePacket;
import de.karlsruhe.hhs.exoplanet.shared.network.protocol.robot.inbound.RobotScanResponsePacket;
import de.karlsruhe.hhs.exoplanet.shared.network.protocol.robot.inbound.StationInfoPacket;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author Yannic Rieger
 */
public class PacketRegistry {

    private final Map<String, Class<? extends Packet>> packets = new HashMap<>();

    public PacketRegistry() {
        this.packets.put("init", InitPacket.class);
        this.packets.put("landed", RobotLandedPacket.class);
        this.packets.put("scaned", RobotScanResponsePacket.class);
        this.packets.put("moved", RobotMoveResponsePacket.class);
        this.packets.put("mvscaned", RobotMoveAndScanResponsePacket.class);
        this.packets.put("rotated", RobotRotateResponsePacket.class);
        this.packets.put("crashed", RobotCrashedPacket.class);
        this.packets.put("info", StationInfoPacket.class);
        this.packets.put("isblocked", FieldBlockedResponsePacket.class);
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
