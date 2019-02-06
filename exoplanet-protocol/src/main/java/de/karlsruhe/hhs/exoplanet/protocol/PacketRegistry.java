package de.karlsruhe.hhs.exoplanet.protocol;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author Yannic Rieger
 */
public class PacketRegistry {

    private final Map<String, Class<? extends Packet>> packets = new HashMap<>();

    static {

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
