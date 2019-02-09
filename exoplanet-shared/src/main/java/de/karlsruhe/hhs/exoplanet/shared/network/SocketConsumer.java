package de.karlsruhe.hhs.exoplanet.shared.network;

import de.karlsruhe.hhs.exoplanet.shared.network.protocol.Packet;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.function.Consumer;

/**
 * @author Yannic Rieger
 */
public class SocketConsumer extends Thread {

    private final Map<Class<? extends Packet>, Consumer<Packet>> consumers = new HashMap<>();
    private final BlockingQueue<Packet> packets;

    public SocketConsumer(final BlockingQueue<Packet> packets) {
        this.packets = packets;
    }


    @Override
    public void run() {
        while (!this.isInterrupted()) {
            try {
                final Packet received = this.packets.take();
                final Consumer<Packet> consumer = this.consumers.get(received.getClass());
                if (consumer == null) continue;
                consumer.accept(received);
            } catch (final InterruptedException e) {
                this.interrupt();
            }
        }
    }

    public <T extends Packet> SocketConsumer consume(final Class<T> clazz, final Consumer<T> consumer) {
        this.consumers.put(clazz, (Consumer<Packet>) consumer);
        return this;
    }

    public void shutdown() throws InterruptedException {
        this.interrupt();
        this.join(); // Wait for this thread to die
    }
}
