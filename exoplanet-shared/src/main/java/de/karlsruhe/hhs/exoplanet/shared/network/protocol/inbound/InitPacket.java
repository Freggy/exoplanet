package de.karlsruhe.hhs.exoplanet.shared.network.protocol.inbound;


import de.karlsruhe.hhs.exoplanet.shared.network.protocol.Packet;
import java.util.List;

/**
 * @author Yannic Rieger
 */
public class InitPacket implements Packet {

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    private int width;
    private int height;
    private Object size;

    // TODO: size

    @Override
    public String encode() {
        return "";
    }

    @Override
    public void decode(final List<String> data) {
        this.size = data.get(0);
        this.width = Integer.valueOf(data.get(1));
        this.height = Integer.valueOf(data.get(2));
    }
}
