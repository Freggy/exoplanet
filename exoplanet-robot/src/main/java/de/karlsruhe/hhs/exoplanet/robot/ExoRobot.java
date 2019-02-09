package de.karlsruhe.hhs.exoplanet.robot;

import de.karlsruhe.hhs.exoplanet.shared.Console;
import de.karlsruhe.hhs.exoplanet.shared.Direction;
import de.karlsruhe.hhs.exoplanet.shared.Position;
import de.karlsruhe.hhs.exoplanet.shared.Size;
import de.karlsruhe.hhs.exoplanet.shared.network.SocketConsumer;
import de.karlsruhe.hhs.exoplanet.shared.network.protocol.robot.inbound.FieldBlockedResponsePacket;
import de.karlsruhe.hhs.exoplanet.shared.network.protocol.robot.inbound.InitPacket;
import de.karlsruhe.hhs.exoplanet.shared.network.protocol.robot.inbound.RobotCrashedPacket;
import de.karlsruhe.hhs.exoplanet.shared.network.protocol.robot.inbound.RobotLandedPacket;
import de.karlsruhe.hhs.exoplanet.shared.network.protocol.robot.inbound.RobotMoveResponsePacket;
import de.karlsruhe.hhs.exoplanet.shared.network.protocol.robot.inbound.StationInfoPacket;
import de.karlsruhe.hhs.exoplanet.shared.network.protocol.robot.outbound.FieldBlockedRequestPacket;
import de.karlsruhe.hhs.exoplanet.shared.network.protocol.robot.outbound.RobotExitPacket;
import de.karlsruhe.hhs.exoplanet.shared.network.protocol.robot.outbound.RobotLandPacket;
import de.karlsruhe.hhs.exoplanet.shared.network.protocol.robot.outbound.RobotMovePacket;
import java.net.InetSocketAddress;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Phaser;

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

    private final Console console;
    private final Set<Position> robotPositionCache = ConcurrentHashMap.newKeySet();
    private UUID id;
    private final CyclicBarrier cyclicBarrier = new CyclicBarrier(2);

    private final SocketConsumer planetConsumer;
    private final SocketConsumer stationConsumer;

    private volatile boolean isWayBlocked = false;

    private Phaser phaser = new Phaser(1);

    public ExoRobot(final Console console, final InetSocketAddress station, final InetSocketAddress planet) {
        this.planetConnector = new ClientConnector(console, planet);
        this.stationConnector = new ClientConnector(console, station);
        this.console = console;
        this.currentPosition = new Position(-1, -1);

        this.planetConsumer = new SocketConsumer(this.planetConnector.getPendingPackets());
        this.planetConsumer.consume(InitPacket.class, packet -> {
            this.fieldSize = packet.getSize();
            this.console.println("[Robot] Größe des Planets: X: " + this.fieldSize.getWidth() + ", Y: " + this.fieldSize.getHeight());
        }).consume(RobotCrashedPacket.class, packet -> {
            this.console.println("[Robot] Crash");
        }).consume(RobotLandedPacket.class, packet -> {
            this.console.println("[Robot] Gelandet!");
            this.console.println("[Robot] Daten: " + packet.getMeasurement());
            this.hasLanded = true;
        }).consume(RobotMoveResponsePacket.class, packet -> {
            this.currentPosition = packet.getPosition();
            this.phaser.arriveAndDeregister();
        });

        this.stationConsumer = new SocketConsumer(this.stationConnector.getPendingPackets());
        this.stationConsumer.consume(FieldBlockedResponsePacket.class, packet -> {

        }).consume(StationInfoPacket.class, packet -> {
            this.id = packet.getUuid();
            try {
                this.cyclicBarrier.await();
            } catch (final InterruptedException | BrokenBarrierException e) {
                e.printStackTrace();
            }
        }).consume(FieldBlockedResponsePacket.class, packet -> {
            this.isWayBlocked = packet.isBlocked();
            this.phaser.arriveAndDeregister();
        });

    }

    public void start() {
        this.planetConnector.connectAndStartReading();
        this.planetConsumer.start();

        this.stationConnector.connectAndStartReading();
        this.stationConsumer.start();

        try {
            this.cyclicBarrier.await();
            this.console.println("[ExoRobot] Starten mit zugewiesener ID: " + this.id);
        } catch (final InterruptedException | BrokenBarrierException e) {
            e.printStackTrace();
        }
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

        final FieldBlockedRequestPacket blocked = new FieldBlockedRequestPacket();
        blocked.setPosition(newPos);


        this.phaser.register();
        this.stationConnector.write(blocked);

        this.phaser.arriveAndAwaitAdvance(); // Wait until the field blocked response has arrived

        if (this.isWayBlocked) {
            this.console.println("[ExoRobot] FEHLER: Bewegung würde Kollision verursachen.");
            return;
        }

        this.phaser.register();
        this.planetConnector.write(new RobotMovePacket());

        this.phaser.arriveAndAwaitAdvance(); // Wait until the move response packet has arrived
        this.phaser.arriveAndDeregister();
        this.phaser = new Phaser(1);
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

        try {
            this.planetConsumer.shutdown();
            this.stationConsumer.shutdown();
        } catch (final InterruptedException e) {
            e.printStackTrace();
        }

        this.stationConnector.disconnect();
        this.planetConnector.disconnect();
    }
}
