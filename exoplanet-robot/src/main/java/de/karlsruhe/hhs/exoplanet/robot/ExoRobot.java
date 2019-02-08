package de.karlsruhe.hhs.exoplanet.robot;

import de.karlsruhe.hhs.exoplanet.shared.Console;
import de.karlsruhe.hhs.exoplanet.shared.Direction;
import de.karlsruhe.hhs.exoplanet.shared.Position;
import de.karlsruhe.hhs.exoplanet.shared.Size;
import de.karlsruhe.hhs.exoplanet.shared.network.protocol.Packet;
import de.karlsruhe.hhs.exoplanet.shared.network.protocol.inbound.InitPacket;
import de.karlsruhe.hhs.exoplanet.shared.network.protocol.inbound.RobotCrashedPacket;
import de.karlsruhe.hhs.exoplanet.shared.network.protocol.inbound.RobotLandedPacket;
import de.karlsruhe.hhs.exoplanet.shared.network.protocol.inbound.RobotMoveAndScanResponsePacket;
import de.karlsruhe.hhs.exoplanet.shared.network.protocol.inbound.RobotRotateResponsePacket;
import de.karlsruhe.hhs.exoplanet.shared.network.protocol.inbound.RobotScanResponsePacket;
import de.karlsruhe.hhs.exoplanet.shared.network.protocol.outbound.RobotExitPacket;
import de.karlsruhe.hhs.exoplanet.shared.network.protocol.outbound.RobotLandPacket;
import java.net.InetSocketAddress;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Yannic Rieger
 */
public class ExoRobot {

    public boolean hasLanded() {
        return this.hasLanded;
    }

    public UUID getId() {
        return this.id;
    }

    public Size getFieldSize() {
        return this.fieldSize;
    }

    private Size fieldSize;
    private boolean hasLanded;
    private Position currentPosition;

    private final ClientConnector planetConnector;
    private final ClientConnector stationConnector;

    private Thread planetThread;
    private final Thread stationThread;

    private final Console console;
    private final Set<Position> robotPositionCache = ConcurrentHashMap.newKeySet();
    private final UUID id = UUID.randomUUID();

    public ExoRobot(final Console console, final InetSocketAddress station, final InetSocketAddress planet) {
        this.planetConnector = new ClientConnector(console, planet);
        this.stationConnector = new ClientConnector(console, station);
        this.console = console;

        this.planetThread = new Thread(() -> {
            while (!this.planetThread.isInterrupted()) {
                try {
                    final Packet received = this.planetConnector.getPendingPackets().take();

                    if (received instanceof InitPacket) {
                        this.fieldSize = ((InitPacket) received).getSize();
                        this.console.println("[Robot] Planet size is:");
                        this.console.println(" Max. Y: " + this.fieldSize.getHeight());
                        this.console.println(" Max. X: " + this.fieldSize.getWidth());
                    } else if (received instanceof RobotCrashedPacket) {
                        this.console.println("[Robot] Crashed");
                    } else if (received instanceof RobotLandedPacket) {
                        final RobotLandedPacket packet = (RobotLandedPacket) received;
                        this.console.println("[Robot] Robot landed!");
                        this.console.println("[Robot] Data: " + packet.getMeasurement());
                        this.hasLanded = true;
                    } else if (received instanceof RobotMoveAndScanResponsePacket) {

                    } else if (received instanceof RobotRotateResponsePacket) {

                    } else if (received instanceof RobotScanResponsePacket) {

                    }
                } catch (final InterruptedException ex) {
                    this.planetThread.interrupt(); // just in case
                }
            }
        });

        this.stationThread = new Thread(() -> {
            while (!this.planetThread.isInterrupted()) {
                final Packet received = this.stationConnector.getPendingPackets().poll();

                // TODO: handle packets from station
            }
        });
    }

    public void start() {
        this.console.println("[ExoRobot] Starting. ID: " + this.id);
        this.planetConnector.connectAndStartReading();
        this.planetThread.start();
    }

    public void move(final Direction direction) {

        int newX = 0;
        int newY = 0;

        switch (direction) {
            case EAST:
                newX = this.moveOnPlane(false, this.currentPosition.getX(), this.fieldSize.getWidth());
                break;

            case WEST:
                newX = this.moveOnPlane(true, this.currentPosition.getX(), this.fieldSize.getWidth());
                break;

            case NORTH:
                newY = this.moveOnPlane(false, this.currentPosition.getY(), this.fieldSize.getHeight());
                break;

            case SOUTH:
                newY = this.moveOnPlane(true, this.currentPosition.getY(), this.fieldSize.getHeight());
                break;
        }

        if (newX == -1 || newY == -1) {
            // TODO: error
            return;
        }

        final Position newPos = new Position(
            this.currentPosition.getX() + newX,
            this.currentPosition.getY() + newY, this.currentPosition.getDir()
        );

        if (this.robotPositionCache.contains(newPos)) {
            // TODO: error
            return;
        }

        // TODO: send packet to station
        // TODO: retrieve packet from station, it contains whether or not the robot can move
        // TODO: send move packet to planet
    }

    private int moveOnPlane(final boolean subtract, final int oldVal, final int max) {
        final int newVal = oldVal + (subtract ? -1 : 1);

        if (newVal < 0 || newVal >= max) {
            return -1;
        }

        return newVal;
    }

    public void land(final int x, final int y) {
        this.currentPosition = new Position(x, y);

        final RobotLandPacket packet = new RobotLandPacket();
        packet.setPosition(this.currentPosition);

        this.planetConnector.write(packet);
    }

    public void destroy() {
        if (!this.hasLanded) return;
        this.planetConnector.write(new RobotExitPacket());
        this.planetThread.interrupt();
        this.stationThread.interrupt();
        this.planetConnector.disconnect();
        //this.stationConnector.disconnect();
    }
}
