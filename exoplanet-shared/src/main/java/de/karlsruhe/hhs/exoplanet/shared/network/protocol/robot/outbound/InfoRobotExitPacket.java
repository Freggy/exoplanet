package de.karlsruhe.hhs.exoplanet.shared.network.protocol.robot.outbound;

import de.karlsruhe.hhs.exoplanet.shared.network.protocol.Packet;
import java.util.List;
import java.util.UUID;

/**
 * @author Yannic Rieger
 */
public class InfoRobotExitPacket implements Packet {

    /**
     *
     */
    public enum Cause {
        CRASH, EXIT
    }

    public Cause getCause() {
        return this.cause;
    }

    public UUID getRobotId() {
        return this.robotId;
    }

    public void setRobotId(final UUID robotId) {
        this.robotId = robotId;
    }

    public void setCause(final Cause cause) {
        this.cause = cause;
    }

    private UUID robotId;
    private Cause cause;

    @Override
    public String encode() {
        return "infoexit:" + this.robotId.toString() + ":" + this.cause.name();
    }

    @Override
    public void decode(final List<String> data) {
        this.robotId = UUID.fromString(data.get(0));
        this.cause = Cause.valueOf(data.get(1));
    }
}
