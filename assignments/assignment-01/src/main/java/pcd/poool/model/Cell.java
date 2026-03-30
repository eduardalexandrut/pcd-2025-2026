package pcd.poool.model;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Cell {
    private int id;
    private final List<Ball> balls;
    private final ReentrantLock lock;
    private final Condition frameProcessed;
    private Boolean isReadyForNextFrame = false;

    public Cell(final int id) {
        this.id = id;
        this.balls = new ArrayList<Ball>();
        this.lock = new ReentrantLock(true);
        this.frameProcessed = lock.newCondition();
    }

    public void processFrame() {
        lock.lock();
        try {
            while (!isReadyForNextFrame) {
                frameProcessed.await();
            }
            isReadyForNextFrame = false;
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

    public int getId() {
        return id;
    }

    public void addBall(Ball ball) {
        this.balls.add(ball);
    }

    public void removeBall(Ball ball) {
        this.balls.remove(ball);
    }


}
