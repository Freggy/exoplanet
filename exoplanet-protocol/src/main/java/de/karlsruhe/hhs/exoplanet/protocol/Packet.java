package de.karlsruhe.hhs.exoplanet.protocol;

/**
 * @author Yannic Rieger
 */
public interface Packet {

    String encode();

    void decode(String[] data);
}
