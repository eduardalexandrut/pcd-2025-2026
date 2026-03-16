package pcd.lab05.monitors.ex_latch;

import javax.management.monitor.Monitor;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MonitorLatch implements Latch{

    private int count;
    private Lock lock;
    private Condition allArrived;

    public MonitorLatch(final int count) {
        this.count = Math.max(count,0);
        lock = new ReentrantLock();
        allArrived = lock.newCondition();
    }

    @Override
    public void await() throws InterruptedException {
        try {
            lock.lock();
            while (count > 0) {
                allArrived.await();
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void countDown() {
        try {
            lock.lock();
            if (count > 0) {
                this.count--;

                if (this.count == 0){
                    allArrived.signalAll();
                }
            }
        } finally {
            lock.unlock();
        }
    }


}
