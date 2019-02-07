package de.karlsruhe.hhs.exoplanet.shared.network.protocol.inbound;


import de.karlsruhe.hhs.exoplanet.shared.Size;
import de.karlsruhe.hhs.exoplanet.shared.network.protocol.Packet;
import java.util.List;

/**
 * @author Yannic Rieger
 */
public class InitPacket implements Packet {

    public Size getSize() {
        return size;
    }

    private Size size;

    @Override
    public String encode() {
        return "";
    }

    @Override
    public void decode(final List<String> data) {
        this.size = Size.parse(data.get(0));
    }
}
