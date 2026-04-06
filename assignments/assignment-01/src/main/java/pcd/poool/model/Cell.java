package pcd.poool.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Cell {
    private final int id;
    private final List<Ball> balls = new ArrayList<>();
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition stateChanged = lock.newCondition();

    // State tracking for the monitor
    private boolean processed = false;

    public Cell(int id) { this.id = id; }

    /**
     * Entry point for the Monitor: Update movement.
     */
    public List<Ball> updateMovement(long dt, Board board, double cellW, double cellH, int r, int c) {
        lock.lock();
        try {
            List<Ball> exited = new ArrayList<>();
            Iterator<Ball> it = balls.iterator();
            while (it.hasNext()) {
                Ball b = it.next();
                b.updateState(dt, board);

                int nr = (int) (b.getPos().y() / cellH);
                int nc = (int) (b.getPos().x() / cellW);
                if (nr != r || nc != c) {
                    exited.add(b);
                    it.remove();
                }
            }
            processed = true;
            stateChanged.signalAll(); // Tell waiting threads movement is done
            return exited;
        } finally {
            lock.unlock();
        }
    }

    public void resolveInternalCollisions() {
        this.lock();

        // Handle internal collisions
        try {
            for (int i = 0; i < this.balls.size(); i++) {
                for (int j = i + 1; j < this.balls.size(); j++) {
                    Ball.resolveCollision(this.balls.get(i), this.balls.get(j));
                }
            }
        } finally {
            this.unlock();
        }
    }

    /**
     * Barrier Logic: Wait until this cell has finished its movement phase.
     */
    public void waitProcessed() throws InterruptedException {
        lock.lock();
        try {
            while (!processed) {
                stateChanged.await();
            }
        } finally {
            lock.unlock();
        }
    }

    public void addBall(Ball b) {
        lock.lock();
        try { balls.add(b); } finally { lock.unlock(); }
    }

    public void reset() {
        this.lock.lock();
        try {
            this.processed = false;
        } finally { this.lock.unlock(); }
    }

    public void lock() { lock.lock(); }
    public void unlock() { lock.unlock(); }
    public List<Ball> getBalls() { return balls; }
    public int getId() { return id; }
}
