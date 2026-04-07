package pcd.poool.model;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SimpleBarrier {
    private int members;
    private int arrivedMembers;
    private Lock lock;
    private Condition allArrived;

    public SimpleBarrier(int members) {
        this.members = members;
        this.arrivedMembers = 0;
        lock = new ReentrantLock();
        allArrived = lock.newCondition();
    }

    public void await() {
        lock.lock();

        try {
            this.arrivedMembers++;

            if (arrivedMembers == members) {
                this.arrivedMembers = 0;
                allArrived.signalAll();
            } else {
                while (arrivedMembers != 0) {
                    allArrived.await();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally { lock.unlock(); }
    }
}
