package de.karlsruhe.hhs.exoplanet.shared.network;

import de.karlsruhe.hhs.exoplanet.shared.network.protocol.Packet;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.function.Consumer;

/**
 * @author Yannic Rieger
 */
public class SocketConsumer {

    private final Map<Class<? extends Packet>, Consumer<Packet>> consumers = new HashMap<>();
    private Thread consumeThread;

    public SocketConsumer(final BlockingQueue<Packet> packets) {
        this.consumeThread = new Thread(() -> {
            while (!this.consumeThread.isInterrupted()) {


                try {
                    final Packet received = packets.take();
                    final Consumer<Packet> consumer = this.consumers.get(received.getClass());

                    if (consumer == null) continue;

                    consumer.accept(received);
                } catch (final InterruptedException e) {
                    this.consumeThread.interrupt();
                }
            }

                /*
                try {
                    final Packet received = packets.take();


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

                    } else if (received instanceof RobotMoveResponsePacket) {
                        this.currentPosition = ((RobotMoveResponsePacket) received).getPosition();
                        try {
                            this.cyclicBarrier.await();
                        } catch (final BrokenBarrierException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (final InterruptedException ex) {
                    this.planetThread.interrupt(); // just in case
                }
            }*/
        });
    }


    public <T extends Packet> SocketConsumer consume(final Class<T> clazz, final Consumer<T> consumer) {
        this.consumers.put(clazz, (Consumer<Packet>) consumer);
        return this;
    }

    public void start() {
        this.consumeThread.start();
    }
}
