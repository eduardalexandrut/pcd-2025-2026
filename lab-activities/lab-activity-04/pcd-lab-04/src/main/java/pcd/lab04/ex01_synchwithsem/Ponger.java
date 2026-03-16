package pcd.lab04.ex01_synchwithsem;

import java.util.concurrent.Semaphore;

public class Ponger extends ActiveComponent {

    Semaphore pingDoneEvent;
    Semaphore pongDoneEvent;

    public Ponger(Semaphore pingDoneEvent, Semaphore pongDoneEvent) {
        this.pingDoneEvent = pingDoneEvent;
        this.pongDoneEvent = pongDoneEvent;
    }

    public void run() {
        while (true){
            try {
                this.pingDoneEvent.acquire();
                wasteRandomTime(500,600);
                println("pong");
//                throw new InterruptedException();
            } catch (InterruptedException ex) {
                log("interrupted..");
            } finally {
                this.pongDoneEvent.release();
//                mutex.release();
            }
        }
    }
}