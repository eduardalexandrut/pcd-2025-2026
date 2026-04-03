package pcd.poool.model;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class PhysicsImpl implements Physics{
    private List<Ball> balls;
    private final Board board;

    public PhysicsImpl(Board board) {
        this.board = board;
        this.balls  = new CopyOnWriteArrayList<>();;
    }

    @Override
    public void computeState(long dt) {

        for (Ball b : balls) {
            b.updateState(dt, board);
        }

        for (int i = 0; i < balls.size(); i++) {
            for (int j = i + 1; j < balls.size(); j++) {
                Ball.resolveCollision(balls.get(i), balls.get(j));
            }
        }
    }

    public void addBall(Ball b) {
        this.balls.add(b);
    }

    @Override
    public void updateUserBall(P2d position) {

    }

    @Override
    public void updateNPCBall(Ball ball) {

    }

    @Override
    public BallState getUserBallState() {
        return null;
    }

    @Override
    public BallState getNPCBallState() {
        return null;
    }

    @Override
    public List<BallState> getStateSnapshot() {
        return this.balls.stream().map(BallState::fromBall).toList();
    }
}
