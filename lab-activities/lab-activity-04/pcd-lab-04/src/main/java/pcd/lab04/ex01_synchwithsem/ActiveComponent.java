package pcd.lab04.ex01_synchwithsem;

import java.util.Random;
import java.util.concurrent.Semaphore;

public abstract class ActiveComponent extends Thread {
    Semaphore mutex;
    Random rand = new Random();

	protected void print(String msg){
		synchronized (System.out){
			System.out.print(msg);
		}
	}

	protected void println(String msg){
		synchronized (System.out){
			System.out.println(msg);
		}
	}

    protected void wasteRandomTime(long min, long max){
        try {
            double value = rand.nextDouble();
            double delay = min + value*(max-min);
            sleep((int)delay);
        } catch (InterruptedException ex){
            ex.printStackTrace();
        }
    }

    protected void log(String msg){
        synchronized (System.out){
            System.out.println("[" + this.getName()+ "] " + msg);
        }
    }

}
