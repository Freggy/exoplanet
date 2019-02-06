package de.karlsruhe.hhs.exoplanet.protocol;

import java.util.List;

/**
 * @author Yannic Rieger
 */
public interface Packet {

    String encode();

    void decode(List<String> data);
}
