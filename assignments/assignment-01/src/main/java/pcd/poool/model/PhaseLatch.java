package pcd.poool.model;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class PhaseLatch {
    private final int totalParts;
    private int currentParts;
    private ReentrantLock lock;
    private Condition allDone;

    public PhaseLatch(int totalParts) {
        this.totalParts = totalParts;
        this.lock = new ReentrantLock();
        this.allDone = lock.newCondition();
        this.currentParts = 0;
    }

    public void awaitPhase() {
        lock.lock();
        try {
            this.currentParts = this.currentParts + 1;

            if (currentParts == totalParts) {
                this.allDone.signalAll();
            } else {
                while (this.currentParts < totalParts) {
                    this.allDone.await();
                }
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }
}
