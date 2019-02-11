package de.karlsruhe.hhs.exoplanet.robot;

import de.karlsruhe.hhs.exoplanet.shared.Console;
import de.karlsruhe.hhs.exoplanet.shared.Direction;
import de.karlsruhe.hhs.exoplanet.shared.Position;
import de.karlsruhe.hhs.exoplanet.shared.Rotation;
import de.karlsruhe.hhs.exoplanet.shared.Size;
import de.karlsruhe.hhs.exoplanet.shared.network.SocketConsumer;
import de.karlsruhe.hhs.exoplanet.shared.network.protocol.bidirectional.RobotPositionUpdatePacket;
import de.karlsruhe.hhs.exoplanet.shared.network.protocol.robot.inbound.InitPacket;
import de.karlsruhe.hhs.exoplanet.shared.network.protocol.robot.inbound.RobotCrashedPacket;
import de.karlsruhe.hhs.exoplanet.shared.network.protocol.robot.inbound.RobotLandedPacket;
import de.karlsruhe.hhs.exoplanet.shared.network.protocol.robot.inbound.RobotMoveAndScanResponsePacket;
import de.karlsruhe.hhs.exoplanet.shared.network.protocol.robot.inbound.RobotMoveResponsePacket;
import de.karlsruhe.hhs.exoplanet.shared.network.protocol.robot.inbound.RobotRotateResponsePacket;
import de.karlsruhe.hhs.exoplanet.shared.network.protocol.robot.inbound.RobotScanResponsePacket;
import de.karlsruhe.hhs.exoplanet.shared.network.protocol.robot.inbound.StationInfoPacket;
import de.karlsruhe.hhs.exoplanet.shared.network.protocol.robot.outbound.InfoRobotExitPacket;
import de.karlsruhe.hhs.exoplanet.shared.network.protocol.robot.outbound.MeasurementPacket;
import de.karlsruhe.hhs.exoplanet.shared.network.protocol.robot.outbound.RobotExitPacket;
import de.karlsruhe.hhs.exoplanet.shared.network.protocol.robot.outbound.RobotLandPacket;
import de.karlsruhe.hhs.exoplanet.shared.network.protocol.robot.outbound.RobotMoveAndScanPacket;
import de.karlsruhe.hhs.exoplanet.shared.network.protocol.robot.outbound.RobotMovePacket;
import de.karlsruhe.hhs.exoplanet.shared.network.protocol.robot.outbound.RobotRotatePacket;
import de.karlsruhe.hhs.exoplanet.shared.network.protocol.robot.outbound.RobotScanPacket;
import java.net.InetSocketAddress;
import java.util.Map;
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

    public Position getCurrentPosition() {
        return this.currentPosition;
    }

    private Size fieldSize;
    private boolean hasLanded;
    private Position currentPosition;

    private final ClientConnector planetConnector;
    private final ClientConnector stationConnector;

    private final Map<UUID, Position> robotPositionCache = new ConcurrentHashMap<>();
    private final Console console;

    private final SocketConsumer planetConsumer;
    private final SocketConsumer stationConsumer;

    private volatile UUID id;

    private final SilentCycliclBarrier cyclicBarrier = new SilentCycliclBarrier(2);

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
            final InfoRobotExitPacket exitPacket = new InfoRobotExitPacket();
            exitPacket.setCause(InfoRobotExitPacket.Cause.EXIT);
            exitPacket.setRobotId(this.id);
            this.stationConnector.write(exitPacket);
            System.exit(0);
        }).consume(RobotLandedPacket.class, packet -> {
            this.console.println("[Robot] Gelandet!");
            this.console.println("[Robot] Daten: " + packet.getMeasurement());

            final MeasurementPacket measurementPacket = new MeasurementPacket();
            measurementPacket.setMeasurement(packet.getMeasurement());
            measurementPacket.setPosition(this.currentPosition);
            this.stationConnector.write(measurementPacket);

            this.hasLanded = true;
        }).consume(RobotMoveResponsePacket.class, packet -> {
            this.currentPosition = packet.getPosition();
            this.cyclicBarrier.await();
        }).consume(RobotMoveAndScanResponsePacket.class, packet -> {
            this.currentPosition = packet.getPosition();

            final MeasurementPacket measurementPacket = new MeasurementPacket();
            measurementPacket.setMeasurement(packet.getMeasurement());
            measurementPacket.setPosition(this.currentPosition);
            this.stationConnector.write(measurementPacket);
            this.cyclicBarrier.await();
        }).consume(RobotRotateResponsePacket.class, packet -> {
            this.currentPosition.setDir(packet.getDirection());
            this.cyclicBarrier.await();
        }).consume(RobotScanResponsePacket.class, packet -> {
            this.console.println("[ExoRobot] Daten: " + packet.getMeasurement());
            final MeasurementPacket measurementPacket = new MeasurementPacket();
            measurementPacket.setMeasurement(packet.getMeasurement());
            measurementPacket.setPosition(this.currentPosition);
            this.stationConnector.write(measurementPacket);
            this.cyclicBarrier.await();
        });

        this.stationConsumer = new SocketConsumer(this.stationConnector.getPendingPackets());
        this.stationConsumer.consume(StationInfoPacket.class, packet -> {
            this.id = packet.getUuid();
            this.cyclicBarrier.awaitThenReset();
        }).consume(RobotPositionUpdatePacket.class, packet -> {
            this.console.println("[ExoRobot] Updated position of " + packet.getRobotId() + " to " + packet.getPosition());
            this.robotPositionCache.put(packet.getRobotId(), packet.getPosition());
        });

    }

    public void start() {
        this.planetConnector.connectAndStartReading();
        this.planetConsumer.start();

        this.stationConnector.connectAndStartReading();
        this.stationConsumer.start();


        this.cyclicBarrier.await();
        this.console.println("[ExoRobot] Starten mit zugewiesener ID: " + this.id);
    }

    public void move(final boolean moveAndScan) {
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
            newX == 0 ? this.currentPosition.getX() : newX,
            newY == 0 ? this.currentPosition.getY() : newY,
            this.currentPosition.getDir()
        );

        if (this.robotPositionCache.values().contains(newPos)) {
            this.console.println("[ExoRobot] FEHLER: Bewegung würde Kollision verursachen.");
            return;
        }

        if (moveAndScan) {
            this.planetConnector.write(new RobotMoveAndScanPacket());
        } else this.planetConnector.write(new RobotMovePacket());

        this.cyclicBarrier.awaitThenReset();
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

        if (this.robotPositionCache.containsValue(this.currentPosition)) {
            this.console.println("[ExoRobot] FEHLER: Landung würde Kollision verursachen.");
            return;
        }

        final RobotLandPacket packet = new RobotLandPacket();
        packet.setPosition(this.currentPosition);
        this.planetConnector.write(packet);

        // Update position of other robots in this stage,
        // so they don't move to a position where this robot will land.
        final RobotPositionUpdatePacket updatePacket = new RobotPositionUpdatePacket();
        updatePacket.setPosition(this.currentPosition);
        updatePacket.setRobotId(this.id);
        this.stationConnector.write(updatePacket);
    }

    public void rotate(final Rotation rotation) {
        final RobotRotatePacket packet = new RobotRotatePacket();
        packet.setRotation(rotation);
        this.planetConnector.write(packet);

        this.cyclicBarrier.awaitThenReset();
    }

    public void scan() {
        this.planetConnector.write(new RobotScanPacket());
        this.cyclicBarrier.awaitThenReset();
    }

    public void destroy() {
        if (!this.hasLanded) return;
        this.planetConnector.write(new RobotExitPacket());

        final InfoRobotExitPacket exitPacket = new InfoRobotExitPacket();
        exitPacket.setCause(InfoRobotExitPacket.Cause.EXIT);
        exitPacket.setRobotId(this.id);

        this.stationConnector.write(exitPacket);

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
