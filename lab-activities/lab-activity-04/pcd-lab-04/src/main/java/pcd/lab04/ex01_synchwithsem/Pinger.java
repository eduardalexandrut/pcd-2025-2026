package pcd.lab04.ex01_synchwithsem;

import java.util.concurrent.Semaphore;

public class Pinger extends ActiveComponent {

    Semaphore pongDoneEvent;
    Semaphore pingDoneEvent;

	public Pinger(Semaphore pongDoneEvent, Semaphore pingDoneEvent) {
        this.pongDoneEvent = pongDoneEvent;
        this.pingDoneEvent = pingDoneEvent;
	}	
	
	public void run() {
        while (true){
            try {
                this.pongDoneEvent.acquire();
                wasteRandomTime(100,600);
                println("ping");
            } catch (InterruptedException ex) {
                log("interrupted..");
            } finally {
                this.pingDoneEvent.release();
//                this.pongDoneEvent.release();
            }
        }
	}
}