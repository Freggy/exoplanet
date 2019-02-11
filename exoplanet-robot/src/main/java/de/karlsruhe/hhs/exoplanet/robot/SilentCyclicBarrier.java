package de.karlsruhe.hhs.exoplanet.robot;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

/**
 * This class more or less wraps the {@link CyclicBarrier}. Only purpose is to silence exceptions when
 * calling {@link CyclicBarrier#await()}.
 *
 * @author Yannic Rieger
 */
public class SilentCyclicBarrier extends CyclicBarrier {

    public SilentCyclicBarrier(final int parties) {
        super(parties);
    }

    @Override
    public int await() {
        try {
            return super.await();
        } catch (final InterruptedException | BrokenBarrierException ex) {
            ex.printStackTrace();
        }
        return -1;
    }

    /**
     * Calls {@link CyclicBarrier#await()} then {@link CyclicBarrier#reset()}.
     */
    public void awaitThenReset() {
        this.await();
        this.reset();
    }
}
