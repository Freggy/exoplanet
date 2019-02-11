package de.karlsruhe.hhs.exoplanet.robot;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

/**
 * @author Yannic Rieger
 */
public class SilentCycliclBarrier extends CyclicBarrier {

    public SilentCycliclBarrier(final int parties) {
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

    public void awaitThenReset() {
        this.await();
        this.reset();
    }
}
