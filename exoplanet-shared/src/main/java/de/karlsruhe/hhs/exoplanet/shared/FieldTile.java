package de.karlsruhe.hhs.exoplanet.shared;

/**
 * @author Yannic Rieger
 */
public class FieldTile {

    private final Position position;
    private final Measure measurement;

    public FieldTile(final Position position, final Measure measurement) {
        this.position = position;
        this.measurement = measurement;
    }

    public Position getPosition() {
        return this.position;
    }

    public Measure getMeasurement() {
        return this.measurement;
    }
}
