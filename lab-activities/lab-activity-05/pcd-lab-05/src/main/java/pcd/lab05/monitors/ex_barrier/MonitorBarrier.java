package pcd.lab05.monitors.ex_barrier;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MonitorBarrier implements Barrier{

    private int nParticipants;
    private int confirmed = 0;
    private Lock lock;
    private Condition allArrived;

    public MonitorBarrier(int nParticipants) {
        this.nParticipants = nParticipants;
        this.lock = new ReentrantLock();
        allArrived = this.lock.newCondition();
    }

    @Override
    public void hitAndWaitAll() throws InterruptedException {
        try {
            lock.lock();
            this.confirmed = this.confirmed + 1;

            if (this.confirmed < this.nParticipants) {
                while (this.confirmed < this.nParticipants) {
                    allArrived.await();
                }
            } else {
                allArrived.signalAll();
            }
        } finally {
            lock.unlock();
        }

    }

}
