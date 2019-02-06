package de.karlsruhe.hhs.exoplanet.protocol.inbound;

import de.karlsruhe.hhs.exoplanet.protocol.Packet;

/**
 * @author Yannic Rieger
 */
public class InitPacket implements Packet {

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    private int width;
    private int height;
    private Object size;

    // TODO: size

    public String encode() {
        return "";
    }

    public void decode(String[] data) {
        this.size = data[0];
        this.width = Integer.valueOf(data[1]);
        this.height = Integer.valueOf(data[1]);
    }
}
