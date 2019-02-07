package de.karlsruhe.hhs.exoplanet.robot;

import de.karlsruhe.hhs.exoplanet.shared.Console;
import de.karlsruhe.hhs.exoplanet.shared.FieldTile;
import de.karlsruhe.hhs.exoplanet.shared.Size;
import de.karlsruhe.hhs.exoplanet.shared.network.protocol.Packet;
import de.karlsruhe.hhs.exoplanet.shared.network.protocol.inbound.InitPacket;
import de.karlsruhe.hhs.exoplanet.shared.network.protocol.inbound.RobotCrashedPacket;
import de.karlsruhe.hhs.exoplanet.shared.network.protocol.inbound.RobotLandedPacket;
import de.karlsruhe.hhs.exoplanet.shared.network.protocol.inbound.RobotMoveAndScanResponsePacket;
import de.karlsruhe.hhs.exoplanet.shared.network.protocol.inbound.RobotRotateResponsePacket;
import de.karlsruhe.hhs.exoplanet.shared.network.protocol.inbound.RobotScanResponsePacket;
import java.net.InetSocketAddress;

/**
 * @author Yannic Rieger
 */
public class ExoRobot {

    private Size fieldSize;
    private FieldTile current;

    private final ClientConnector planetConnector;
    private final ClientConnector stationConnector;

    private Thread planetThread;
    private final Thread stationThread;

    private final Console console;

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
                    } else if (received instanceof RobotCrashedPacket) {

                    } else if (received instanceof RobotLandedPacket) {

                    } else if (received instanceof RobotMoveAndScanResponsePacket) {

                    } else if (received instanceof RobotRotateResponsePacket) {

                    } else if (received instanceof RobotScanResponsePacket) {

                    }
                } catch (final InterruptedException ex) {
                    this.planetThread.interrupt();
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
        this.console.println("[ExoRobot] Starting...");
        this.planetConnector.connectAndStartReading();
        this.planetThread.start();
    }

    public void move(final Object direction) {

    }

    public void destroy() {
        this.planetThread.interrupt();
        this.stationThread.interrupt();
        this.planetConnector.disconnect();
        //this.stationConnector.disconnect();
    }
}
