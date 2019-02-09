package de.karlsruhe.hhs.exoplanet.robot;

import de.karlsruhe.hhs.exoplanet.shared.Console;
import de.karlsruhe.hhs.exoplanet.shared.Direction;
import de.karlsruhe.hhs.exoplanet.shared.Position;
import de.karlsruhe.hhs.exoplanet.shared.Size;
import de.karlsruhe.hhs.exoplanet.shared.network.SocketConsumer;
import de.karlsruhe.hhs.exoplanet.shared.network.protocol.Packet;
import de.karlsruhe.hhs.exoplanet.shared.network.protocol.robot.inbound.InitPacket;
import de.karlsruhe.hhs.exoplanet.shared.network.protocol.robot.inbound.RobotCrashedPacket;
import de.karlsruhe.hhs.exoplanet.shared.network.protocol.robot.inbound.RobotLandedPacket;
import de.karlsruhe.hhs.exoplanet.shared.network.protocol.robot.inbound.RobotMoveResponsePacket;
import de.karlsruhe.hhs.exoplanet.shared.network.protocol.robot.outbound.RobotExitPacket;
import de.karlsruhe.hhs.exoplanet.shared.network.protocol.robot.outbound.RobotLandPacket;
import de.karlsruhe.hhs.exoplanet.shared.network.protocol.robot.outbound.RobotMovePacket;
import java.net.InetSocketAddress;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CyclicBarrier;

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

    public Position getCurrentPosition() {
        return this.currentPosition;
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
    private final CyclicBarrier cyclicBarrier = new CyclicBarrier(2);

    private final SocketConsumer consumer;

    public ExoRobot(final Console console, final InetSocketAddress station, final InetSocketAddress planet) {
        this.planetConnector = new ClientConnector(console, planet);
        this.stationConnector = new ClientConnector(console, station);
        this.console = console;
        this.currentPosition = new Position(-1, -1);

        this.consumer = new SocketConsumer(this.planetConnector.getPendingPackets());

        this.consumer.consume(InitPacket.class, packet -> {
            this.fieldSize = packet.getSize();
            this.console.println("[Robot] Planet size is:");
            this.console.println(" Max. Y: " + this.fieldSize.getHeight());
            this.console.println(" Max. X: " + this.fieldSize.getWidth());
        }).consume(RobotCrashedPacket.class, packet -> {
            this.console.println("[Robot] Crashed");
        }).consume(RobotLandedPacket.class, packet -> {
            this.console.println("[Robot] Robot landed!");
            this.console.println("[Robot] Data: " + packet.getMeasurement());
            this.hasLanded = true;
        }).consume(RobotMoveResponsePacket.class, packet -> {
            this.currentPosition = packet.getPosition();
            try {
                this.cyclicBarrier.await();
            } catch (final BrokenBarrierException | InterruptedException e) {
                e.printStackTrace();
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
        //this.planetThread.start();
        this.consumer.start();
    }

    public void move() {
        int newX = 0;
        int newY = 0;

        switch (this.currentPosition.getDir()) {
            case EAST:
                newX = this.moveOnPlane(false, this.currentPosition.getX(), this.fieldSize.getWidth());
                break;

            case WEST:
                newX = this.moveOnPlane(true, this.currentPosition.getX(), this.fieldSize.getWidth());
                break;

            case NORTH:
                newY = this.moveOnPlane(true, this.currentPosition.getY(), this.fieldSize.getHeight());
                break;

            case SOUTH:
                newY = this.moveOnPlane(false, this.currentPosition.getY(), this.fieldSize.getHeight());
                break;
        }

        if (newX == -1 || newY == -1) {
            this.console.println("[ExoRobot] FEHLER: Bewegung würde Spielfeld überschreiten.");
            return;
        }

        final Position newPos = new Position(
            this.currentPosition.getX() + newX,
            this.currentPosition.getY() + newY, this.currentPosition.getDir()
        );

        if (this.robotPositionCache.contains(newPos)) {
            this.console.println("[ExoRobot] FEHLER: Bewegung würde Kollision verursachen.");
            return;
        }

        this.planetConnector.write(new RobotMovePacket());

        try {
            this.cyclicBarrier.await();
        } catch (final BrokenBarrierException | InterruptedException e) {
            e.printStackTrace();
        }

        this.cyclicBarrier.reset();

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

    public void land(final int x, final int y, final Direction direction) {
        this.currentPosition = new Position(x, y, direction);

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
