package de.karlsruhe.hhs.exoplanet.robot;

import com.sun.org.apache.xml.internal.security.Init;
import de.karlsruhe.hhs.exoplanet.protocol.ClientConnector;
import de.karlsruhe.hhs.exoplanet.protocol.Packet;
import de.karlsruhe.hhs.exoplanet.protocol.inbound.InitPacket;
import de.karlsruhe.hhs.exoplanet.protocol.inbound.RobotCrashedPacket;
import de.karlsruhe.hhs.exoplanet.protocol.inbound.RobotLandedPacket;
import de.karlsruhe.hhs.exoplanet.protocol.inbound.RobotMoveAndScanResponsePacket;
import de.karlsruhe.hhs.exoplanet.protocol.inbound.RobotRotateResponsePacket;
import de.karlsruhe.hhs.exoplanet.protocol.inbound.RobotScanResponsePacket;

/**
 * @author Yannic Rieger
 */
public class ExoRobot {

    private FieldTile[][] field;
    private FieldTile current;

    private ClientConnector planetConnector;
    private ClientConnector stationConnector;

    private Thread planetThread;
    private Thread stationThread;

    public ExoRobot() {
        this.planetThread = new Thread(() -> {
            while (!this.planetThread.isInterrupted()) {
                Packet received = this.planetConnector.getPendingPackets().poll();

                if (received instanceof InitPacket) {
                    InitPacket packet = (InitPacket) received;
                    this.field = new FieldTile[packet.getHeight()][packet.getWidth()];

                } else if (received instanceof RobotCrashedPacket) {

                } else if (received instanceof RobotLandedPacket) {

                } else if (received instanceof RobotMoveAndScanResponsePacket) {

                } else if (received instanceof RobotRotateResponsePacket) {

                } else if (received instanceof RobotScanResponsePacket) {

                }
            }
        });

        this.stationThread = new Thread(() -> {
            while (!this.planetThread.isInterrupted()) {
                Packet received = this.stationConnector.getPendingPackets().poll();

                // TODO: handle packets from station
            }
        });
    }

    public void start() {
        this.planetConnector.connectAndStartReading();
    }

    public void move(Object direction) {

    }
}
