package de.karlsruhe.hhs.exoplanet.station;

import java.util.concurrent.Phaser;

/**
 * @author Yannic Rieger
 */
public class Main {

    static private final Phaser phaser = new Phaser(1);

    static Thread thread1;

    static Thread thread2;


    public static void main(final String[] args) {


        /*
        while (true) {

            phaser = new Phaser(1);

            thread2 = new Thread(() -> {
                try {
                    Thread.currentThread().sleep(3000);
                } catch (final InterruptedException e) {
                    e.printStackTrace();
                }

                System.out.println("2");
                phaser.arriveAndDeregister();
            });

            thread1 = new Thread(() -> {
                try {
                    Thread.currentThread().sleep(2000);
                } catch (final InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("1");
                phaser.arriveAndDeregister();
            });


            System.out.println("TEIL 1");
            thread1.start();
            phaser.register();
            phaser.arriveAndAwaitAdvance();

            System.out.println("TEIL 2");
            phaser.register();
            thread2.start();

            phaser.arriveAndAwaitAdvance();


            System.out.println("TEIL 3 DERIGSTER");
            phaser.arriveAndDeregister();
        }*/
    }
}
