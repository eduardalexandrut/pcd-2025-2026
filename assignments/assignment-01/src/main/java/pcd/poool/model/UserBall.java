package pcd.poool.model;

import java.util.concurrent.locks.ReentrantLock;

public class UserBall extends Ball {
    private final ReentrantLock lock = new ReentrantLock();

    public UserBall(P2d pos, double radius, double mass, V2d vel) {
        super(pos, radius, mass, vel);
    }

    public void setPosition(P2d pos) {
        this.lock.lock();
        try {
            this.setPosition(pos);
        } finally {
            this.lock.unlock();
        }
    }

    @Override
    public P2d getPos() {
        this.lock.lock();
        try {
            P2d pos = super.getPos();
            return new P2d(pos.x(), pos.y());
        } finally { this.lock.unlock(); }
    }
}
